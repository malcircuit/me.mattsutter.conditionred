package me.mattsutter.conditionred.util;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

import me.mattsutter.conditionred.graphics.Animation;
import me.mattsutter.conditionred.graphics.FrameChangeCommand;
import me.mattsutter.conditionred.graphics.NewFrameCommand;
import me.mattsutter.conditionred.graphics.RenderCommand;
import me.mattsutter.conditionred.products.RadarProduct;

import static me.mattsutter.conditionred.products.ProductDescriptionBlock.MJDtoCal;
import static me.mattsutter.conditionred.products.ProductDescriptionBlock.secToHour;
import static me.mattsutter.conditionred.products.RadarProduct.MAX_SCAN_NUM;
//import static me.mattsutter.conditionred.products.RadarProduct.MIN_SCAN_NUM;
import static me.mattsutter.conditionred.util.NWSFile.MAX_SEQ_NUM;

public class ProductManager{

	private static final int INVALID_SEQ = -1;
	private final static int CORE_POOL_SIZE = 5;
	private static final long POST_DELAY = 100;
	private static final long FRAME_INTERVAL = 150;
	
	private final ScheduledThreadPoolExecutor executor;
	private final ArrayList<Future<RadarProduct>> pending;
	private final CacheManager cache_man;
	private Animation animation;
	private final int frame_num;
	private final Handler handler;
	private final ConcurrentLinkedQueue<RenderCommand> queue;
	
	private int server_seq_start = INVALID_SEQ;
	private boolean auto_poll;
	private Time system_date = new Time();
	private ScheduledFuture<?> frame_change;
	private boolean anim_running = false;
	
	public ProductManager(Context context, int frames, boolean auto_poll, Handler handler,
			ConcurrentLinkedQueue<RenderCommand> queue){
		frame_num = frames;
		this.queue = queue;
		this.auto_poll = auto_poll;
		this.handler = handler;
		
		executor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
		pending = new ArrayList<Future<RadarProduct>>();
		cache_man = new CacheManager(context);
		animation = new Animation(frames);
	}
	
	public void cancelAll(){
		for (Future<RadarProduct> future : pending)
			future.cancel(true);
		pending.clear();
	}

	public void addProduct(RadarProduct product){
		synchronized(animation){
			if (server_seq_start == product.server_seq_num)
				animation.updateAnimStart(product.prod_block.vol_scan_num);
			final int index = animation.addFrame(product);
			
			queue.add(new NewFrameCommand(product, index));
		}

	}
	
	public RadarProduct getCurrentFrame(){
		return animation.getCurrentFrame();
	}
	
	public void updateSeqStart(int server_seq_num){
		if (server_seq_start != MAX_SEQ_NUM && server_seq_num > server_seq_start){
			server_seq_start = server_seq_num;
			Log.d("Product Manager", "New Sequence start: " + Integer.toString(server_seq_num));
		}
		else if (server_seq_start == MAX_SEQ_NUM && server_seq_num < server_seq_start){
			server_seq_start = server_seq_num;
			Log.d("Product Manager", "New Sequence start: " + Integer.toString(server_seq_num));
		}
	}
	
	public void productChange(String prod_url, String site){
		cancelAll();
		stopAnimation();
		animation.reset();
		
		server_seq_start = INVALID_SEQ;
		pending.add(executor.submit(new ProductDownload(handler, site, prod_url, server_seq_start, auto_poll)));
	}
	
	public void startAnimation(){
		if (!anim_running){
			frame_change = executor.scheduleAtFixedRate(startAnimation, 0, FRAME_INTERVAL, TimeUnit.MILLISECONDS);
			anim_running = true;
		}
	}
	
	public void stopAnimation(){
		if (anim_running){
			frame_change.cancel(true);
			anim_running = false;
		}
	}
	
	public void toggleAutoPoll(boolean auto_poll){
		this.auto_poll = auto_poll;
	}
	
	public void onResume(){
		cache_man.open();
	}
	
	public void onDestroy(){
		cache_man.close();
	}
	
	public void onConnectionLost(){
		// TODO implement
	}
	
	public void onConnection(){
		// TODO implement
	}
	
	public void onNetworkChange(){
		// TODO implement
	}
	
	private long getExpirationDelayInMillis(int prod_gen_date, int prod_gen_time, int vcp){
		int[] date = MJDtoCal(prod_gen_date);
		int[] time = secToHour(prod_gen_time);
		Time gen_time = new Time(Time.TIMEZONE_UTC);
		gen_time.set(time[2], time[1], time[0], date[1], date[0] - 1, date[2]);
		gen_time.normalize(true);
		system_date.setToNow();
		system_date.normalize(true);
		assert system_date.after(gen_time);
		long delay = (gen_time.toMillis(true) + RadarProduct.refreshTime(vcp)) - system_date.toMillis(true);
		Log.i("Cache", "Current system date: " 
				+ system_date.format3339(false) 
				+ "\nGen date: " + gen_time.format3339(false));
		return delay;
	}
	
	private class ProductDownload implements Callable<RadarProduct>{
		
		private final String prod_url, site;
		private final int prod_code;
		private final Handler handler;
		private final int server_seq_num;
		private final boolean polling;
		
		public ProductDownload(Handler handler, String site, String prod_url, 
				int server_seq_num, boolean auto_poll){
			this.site = site;
			this.prod_url = prod_url;
			prod_code = DatabaseQuery.getProductCode(prod_url);
			this.handler = handler;
			this.server_seq_num = server_seq_num;
			polling = auto_poll;
		}

		public RadarProduct call(){
			RadarProduct product = null;
			if (server_seq_num == INVALID_SEQ){
				int server_seq_start;
				try {
					server_seq_start = NWSFile.findMostRecent(site, prod_url);
					product = new RadarProduct(site, prod_code, prod_url, server_seq_start);
					handler.post(new SeqUpdate(server_seq_start));
					
					product.getProduct();
				}
				catch (InterruptedException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (Exception e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				handler.postDelayed(downloadComplete, POST_DELAY);
				
				// TODO Schedule download for next image.
				
				if (polling)
					handler.postDelayed(new QueueDownloads(site, prod_url), POST_DELAY);
			}
			else{
				product = new RadarProduct(site, prod_code, prod_url, server_seq_num);
				
				try {
					product.getProduct();
				} catch (Exception e) {
					// TODO Download failed, do something
					e.printStackTrace();
				}
				
				handler.postDelayed(downloadComplete, POST_DELAY);
			}
			
			return product;
		}
	}
	
	/**
	 * Must be run in UI thread.
	 * @author Matt
	 *
	 */
	private class SeqUpdate implements Runnable{
		
		private final int server_seq_num;
		
		public SeqUpdate(int server_seq_num){
			this.server_seq_num = server_seq_num;
		}

		public void run() {
			updateSeqStart(server_seq_num);
		}
		
	}
	
	private class LoadFromCache implements Callable<RadarProduct>{
		
		private final String prod_url, site;
		private final int prod_code, prod_angle;
		private final Handler handler;
		private final int vol_scan_num;
		
		public LoadFromCache(Handler handler, String site, String prod_url, int scan_num){
			this.site = site;
			this.prod_url = prod_url;
			prod_code = DatabaseQuery.getProductCode(prod_url);
			prod_angle = DatabaseQuery.getProductAngleFromURL(prod_url);
			this.handler = handler;
			this.vol_scan_num = scan_num;
		}

		public RadarProduct call() throws Exception {
			RadarProduct product = new RadarProduct(site, prod_code, prod_url, prod_code);
			product.getProduct(cache_man.loadFromCache(prod_code, site, prod_angle, vol_scan_num));

			handler.postDelayed(downloadComplete, POST_DELAY);
			
			return product;
		}
	}
	
	/**
	 * Must be run in UI thread.
	 * @author Matt
	 *
	 */
	private class QueueDownloads implements Runnable{
		
		private final int prod_code;
		private final int prod_angle;
		private final String site;
		private final String prod_url;
		
		public QueueDownloads(String site, String prod_url){
			prod_angle = DatabaseQuery.getProductAngleFromURL(prod_url);
			prod_code = DatabaseQuery.getProductCode(prod_url);
			this.site = site;
			this.prod_url = prod_url;
		}

		public void run() {
			final int seq_start = animation.anim_start;
			int server_seq_num, vol_scan_num;
			
			for (int i = 1; i < frame_num; i++){
				server_seq_num = (server_seq_start - i + MAX_SEQ_NUM) % MAX_SEQ_NUM;
				vol_scan_num = (seq_start - i + MAX_SCAN_NUM) % MAX_SCAN_NUM;
				
				if (cache_man.isInCache(prod_code, site, prod_angle, vol_scan_num))
					pending.add(executor.submit(new LoadFromCache(handler, site, prod_url, vol_scan_num)));
				else
					pending.add(executor.submit(
							new ProductDownload(handler, site, prod_url, server_seq_num, auto_poll)
							));
			}
		}
		
	};
	
	private final Runnable startAnimation = new Runnable(){

		public void run() {
			queue.add(new FrameChangeCommand(animation.previousFrame()));
		}
		
	};
	
	private final Runnable downloadComplete = new Runnable(){

		public void run() {
			for (int i = 0; i < pending.size(); i++){
				Future<RadarProduct> future = pending.get(i);
				if (!future.isCancelled() && future.isDone()){
					try {
						RadarProduct product = future.get();
						addProduct(product);
						// TODO: cache product
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Do something if download fails
						e.printStackTrace();
					}
					
					pending.remove(i);
				}
			}
		}
	};
	
}


