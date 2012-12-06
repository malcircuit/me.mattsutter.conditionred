package me.mattsutter.conditionred.products;

import static me.mattsutter.conditionred.products.ProductDescriptionBlock.MJDtoCal;
import static me.mattsutter.conditionred.products.ProductDescriptionBlock.secToHour;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import android.text.format.Time;

import me.mattsutter.conditionred.util.NWSFile;

public class RadarProduct {	
	public static enum Type { RADIAL, RASTER, ALPHA_NUM };
	
	public static final int BASE_REFL = 19;			// radial
	public static final int BASE_REFL_LONG = 20;	// radial
	public static final int BASE_VEL = 25;			// radial
	public static final int BASE_SW_SHORT = 28;		// radial
	public static final int BASE_SW_LONG = 30;		// radial
	public static final int D_HYB_SCAN_REFL = 32;	// radial
	public static final int CLUTTER = 34;			// radial
	public static final int COMP_REFL_SHORT = 37;	// raster
	public static final int COMP_REFL_LONG = 38;	// raster
	public static final int RAD_CODE_MSG = 74;		// alpha-numeric
	public static final int FREE_TXT_MSG = 75;		// alpha-numeric
	public static final int ECHO_TOPS = 41;			// raster
	public static final int SRV = 56;				// radial
	public static final int VIL = 57;				// raster
	public static final int STRM_TRACK = 58;		// alpha-numeric/geographic
	public static final int HAIL_INDEX = 59;		// alpha-numeric/geographic
	public static final int TVS = 61;				// alpha-numeric/geographic
	public static final int STRM_STRUCT = 62;		// alpha-numeric/geographic
	public static final int LAYER_COMP_REFL_MAX = 65;//raster
	//public static final int LAYER_COMP_REFL_MAX_MID = 65;
	//public static final int LAYER_COMP_REFL_MAX_HI = 65;
	public static final int RAIN_TOTAL_1HR = 78;	// radial
	public static final int RAIN_TOTAL_3HR = 79;	// radial
	public static final int RAIN_TOTAL_STRM = 80;	// radial
	public static final int E_BASE_REFL = 94;		// radial
	public static final int E_BASE_VEL = 99;		// radial
	public static final int D_VIL = 134;			// radial
	public static final int E_ECHO_TOPS = 135;		// radial
	public static final int D_RAIN_TOTAL_STRM = 138;// radial
	public static final int MESO = 141;				// alpha-numeric/geographic
	
//	private static final String SRV_ANGLE_1 = "DS.56rm0/";
//	private static final String SRV_ANGLE_2 = "DS.56rm1/";
//	private static final String SRV_ANGLE_3 = "DS.56rm2/";
//	//private static final String SRV_ANGLE_4 = "DS.56rm3/";
//	
//	private static final String VEL_ANGLE_1 = "DS.p99v0/";
//	private static final String VEL_ANGLE_2 = "DS.p99v1/";
//	private static final String VEL_ANGLE_3 = "DS.p99v2/";
//	private static final String VEL_ANGLE_4 = "DS.p99v3/";
	
	// Volume Scan Number is a counter that recycles to 1 every 80 scans.  So that means 1 to 80, inclusive.
	public static final int MIN_SCAN_NUM = 1;
	public static final int MAX_SCAN_NUM = 80;

	public static final int CACHED = -1;
	
	public MessageHeader header;
	public ProductDescriptionBlock prod_block;
//	private SymbBlock symb;
	public RadialLayer radial_layer;
	
	public final String url;
	public final String site;
	public final int prod_code;
	private final boolean debug;
	public final int server_seq_num;
	public final Type type;
	
	public RadarProduct(String _site, int prod_code, String _url, int server_seq_num){
		debug = false;
		site = _site;
		this.prod_code = prod_code;
		url = _url;
		this.server_seq_num = server_seq_num;
		type = getType(prod_code);
	}
	
	public RadarProduct(String _site, int prod_code, String _url){
		debug = false;
		site = _site;
		this.prod_code = prod_code;
		url = _url;
		this.server_seq_num = CACHED;
		type = getType(prod_code);
	}
	
	public RadarProduct(int prod_code){
		debug = true;
		site = null;
		this.prod_code = prod_code;
		url = null;
		server_seq_num = CACHED;
		type = getType(prod_code);
	}
	
	public void getProduct() throws IOException, URISyntaxException, InterruptedException{
		if (!debug){
			DataInputStream product = NWSFile.getProductfile(url, site, server_seq_num);
			header = new MessageHeader(product);
			prod_block = new ProductDescriptionBlock(product);
			loadProduct(product);
		}
	}
	
	public void getProduct(DataInputStream product) throws InterruptedException, IOException{
			header = new MessageHeader(product);
			prod_block = new ProductDescriptionBlock(product);
			loadProduct(product);
	}

	public void getDebugProduct(String file_name) throws Exception{
		if (debug){
			DataInputStream product = NWSFile.getDebugProduct(file_name);
			header = new MessageHeader(product);
			prod_block = new ProductDescriptionBlock(product);
			loadProduct(product);
		}
	}

	public static long refreshTime(int vcp){
    	switch (vcp){
    	case 12:
    	case 212:
    		return (long) (4.5f * 60.0f * 1000.0f);
    	case 121:
    		return (long) (6 * 60 * 1000);
    	case 31:
    	case 32:
    		return (long) (10 * 60 * 1000);
    	case 11:
    	case 211:
    	case 21:
    	case 221:
    	default:
    		return (long) (5 * 60 * 1000);
    			
    	}
	}
	
	public static Type getType(int prod_code){
		switch (prod_code){
		case COMP_REFL_SHORT:
		case COMP_REFL_LONG:
		case ECHO_TOPS:
		case VIL:
		case LAYER_COMP_REFL_MAX:
			return Type.RASTER;
		case RAD_CODE_MSG:
		case FREE_TXT_MSG:
		case STRM_TRACK:
		case HAIL_INDEX:
		case TVS:
		case STRM_STRUCT:
		case MESO:
			return Type.ALPHA_NUM;
		default:
			return Type.RADIAL;
		}
	}
	
	public long getExpirationDate(){
		final int[] date = MJDtoCal(prod_block.prod_gen_date);
		final int[] time = secToHour(prod_block.prod_gen_time);
		Time gen_time = new Time(Time.TIMEZONE_UTC);
		gen_time.set(time[2], time[1], time[0], date[1], date[0] - 1, date[2]);
		gen_time.normalize(true);
		return gen_time.toMillis(true) + refreshTime(prod_block.vcp);
	}

	
	private void loadProduct(DataInputStream product) throws InterruptedException, IOException {
		switch (type){
		case RADIAL:
			loadRadialProduct(product);
			break;
		case RASTER:
		case ALPHA_NUM:
		}
	}
	
	private void loadRadialProduct(DataInputStream file) throws InterruptedException, IOException{
		radial_layer = new RadialLayer();

		if (prod_block.p[7] == 1){
			CompressedSymbBlock comp_symb = new CompressedSymbBlock();
			comp_symb.readSymbBlock(file, header.mes_len, radial_layer);
		}
		else
			radial_layer.readRadialLayer(file);

		file.close();
	}
	
//	public void getProduct() throws Exception{
//	if (!debug){
//		final DataInputStream product;
//
//		header = new MessageHeader();
//		prod_block = new ProductBlock();
//		radial_layer = new RadialLayer();
//
//		if (prod_type == SRV){
//			//			String base_url;
//			//			DataInputStream bv_product;
//			//			if (url.equals(SRV_ANGLE_1))
//			//				base_url = VEL_ANGLE_1;
//			//			else if (url.equals(SRV_ANGLE_2))
//			//				base_url = VEL_ANGLE_2;
//			//			else if (url.equals(SRV_ANGLE_3))
//			//				base_url = VEL_ANGLE_3;
//			//			else
//			//				base_url = VEL_ANGLE_4;
//
//			product = NWSFile.getProductfile(this.site, this.url, false);
//
//			//			try{
//			//				bv_product = NWSFile.getProductfile(E_BASE_VEL, this.site, base_url, false);
//			//			} 
//			//			catch (Exception e){
//			// Reading MUST be done in this order.
//			header.readWMOHeader(product);
//			header.readMessageHeader(product);
//			prod_block.readProductBlock(product);
////			SymbBlock symb = new SymbBlock();
////			symb.readProductBlock(product);
//			radial_layer.readRadialLayer(product);
//
//			product.close();
//			return;
//			//			}
//			//			
//			//			MessageHeader temp_header = new MessageHeader();
//			//			ProductBlock temp_prod_block = new ProductBlock();
//			//			
//			//			// Reading MUST be done in this order.
//			//			header.readWMOHeader(product);
//			//			header.readMessageHeader(product);
//			//			prod_block.readProductBlock(product);
//			//			
//			//
//			//			temp_header.readWMOHeader(bv_product);
//			//			temp_header.readMessageHeader(bv_product);
//			//			temp_prod_block.readProductBlock(bv_product);
//			//			
//			//			prod_block.thresh[0] = temp_prod_block.thresh[0];
//			//			prod_block.thresh[1] = temp_prod_block.thresh[1];
//			//			
//			//			comp_symb = new CompressedSymbBlock();
//			//			comp_symb.readSRVSymbBlock(
//			//					bv_product, 
//			//					temp_header.mes_len, 
//			//					radial_layer,
//			//					prod_block.thresh[1], 
//			//					((float)prod_block.p[7])/10.0f, 
//			//					((float)prod_block.p[8])/10.0f
//			//					);
//			//
//			//			bv_product.close();
//		}
//		else
//			loadProduct(NWSFile.getProductfile(this.site, this.url, false));
//	}
//}
}
