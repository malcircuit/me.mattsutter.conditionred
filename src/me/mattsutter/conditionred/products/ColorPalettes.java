package me.mattsutter.conditionred.products;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;
//import android.util.Log;

import static me.mattsutter.conditionred.util.NWSFile.DATA_DIR;

public class ColorPalettes{

	// Directory and file names.
	public static final String REFL_PAL = "/reflectivity.pal";
	public static final String VEL_PAL = "/velocity.pal";
	public static final String VIL_PAL = "/vil.pal";
	public static final String SW_PAL = "/spectrum_width.pal";
	public static final String ET_PAL = "/echotops.pal";
	public static final String ONE_HR_PAL = "/onehourrain.pal";
	public static final String STRM_TOTAL_PAL = "/stormtotal.pal";
	public static final String SRV_PAL = "/stormrelative.pal";
	

	public static final String DEFAULT_REFL_PAL = 	"; Default Base Reflectivty color palette.\n"+
													"; Feel free to replace this with a custom color palette of your choice.\n"+
													"; Just make sure it is named \"reflectivity.pal\" and that you put it in this folder.\n"+
													"; If you want to make your own, make sure you consult:\n"+
													"; http://www.allisonhouse.com/grlevelx/manual/grlevel3/files_color_table.htm\n\n"+
													"Units: DBZ\n"+
													"Step: 10\n\n"+
													"Color:  80   128   128   128 \n"+
													"Color:  70   255   255   255 \n"+
													"Color:  60   255     0   255    128    0  128 \n"+
													"Color:  50   255     0     0    160    0    0 \n"+
													"Color:  40   255   255     0    255  128    0\n"+
													"Color:  30     0   255     0      0  128    0\n"+
													"Color:  20    64   128   255     32   64  128 \n"+
													"Color:  10   164   164   255    100  100  192\n";
	
	public static final String DEFAULT_VEL_PAL = 	"; Default Base Velocity color palette.\n"+
													"; Feel free to replace this with a custom color palette of your choice.\n"+
													"; Just make sure it is named \"velocity.pal\" and that you put it in this folder.\n"+
													"; If you want to make your own, make sure you consult:\n"+
													"; http://www.allisonhouse.com/grlevelx/manual/grlevel3/files_color_table.htm\n\n"+
													"Units: KTS\n"+
													"Step:  10\n\n"+
													"RF: 64  0  64\n"+
													"Color:   70    255    0   0\n"+
													"Color:    0     96    0   0\n"+
													"Color:  -70      0  255   0     0  96   0\n";
	
	public static final String DEFAULT_SRV_PAL = 	"; Default Storm Relative Velocity color palette.\n"+
													"; Feel free to replace this with a custom color palette of your choice.\n"+
													"; Just make sure it is named \"stormrelative.pal\" and that you put it in this folder.\n"+
													"; If you want to make your own, make sure you consult:\n"+
													"; http://www.allisonhouse.com/grlevelx/manual/grlevel3/files_color_table.htm\n"+
													"Units: KTS\n"+
													"Step:  10\n"+
													"RF:  64  0  64\n"+

													"Color:   70   255   0   0 \n"+
													"Color:   40   192   0   0     255   0   0\n"+
													"Color:    0   128  64  64     255 128 128\n"+
													"Color:  -40    96 192 128      48  96  96\n"+
													"Color:  -70     0 255   0       0 160   0\n";
													
	public static final String DEFAULT_VIL_PAL = 	"; Default VIL color palette.\n"+
													"; Feel free to replace this with a custom color palette of your choice.\n"+
													"; Just make sure it is named \"vil.pal\" and that you put it in this folder.\n"+
													"; If you want to make your own, make sure you consult:\n"+
													"; http://www.allisonhouse.com/grlevelx/manual/grlevel3/files_color_table.htm\n\n"+
													"Units: kg/m2\n"+
													"Step:  10\n\n"+
													"Color: 10    0 200 255     0  64 128\n"+
													"Color: 20    0 255 200     0 128 100\n"+
													"Color: 30  245 245   0   180 128   0\n"+
													"Color: 40  255 128   0   128  64   0\n"+
													"Color: 50  230   0   0   128   0   0\n"+
													"Color: 60  255   0 255   128   0 128\n"+
													"Color: 70  255 255 255   128 128 128\n"+
													"Color: 80  128 128 128\n";
	
	public static final String DEFAULT_ET_PAL = 	"; Default Echo Tops color palette.\n"+
													"; Feel free to replace this with a custom color palette of your choice.\n"+
													"; Just make sure it is named \"echotops.pal\" and that you put it in this folder.\n"+
													"; If you want to make your own, make sure you consult:\n"+
													"; http://www.allisonhouse.com/grlevelx/manual/grlevel3/files_color_table.htm\n\n"+	
													"Units: KFT\n"+
													"Step:  10\n\n"+
													"Color: 10    0 200 255     0  64 128\n"+
													"Color: 20    0 255 200     0 128 100\n"+
													"Color: 30  245 245   0   180 128   0\n"+
													"Color: 40  255 128   0   128  64   0\n"+
													"Color: 50  230   0   0   128   0   0\n"+
													"Color: 60  255   0 255   128   0 128\n"+
													"Color: 70  255 255 255\n";
	
	public static final String DEFAULT_SW_PAL = 	"; Default Spectrum Width color palette.\n"+
													"; Feel free to replace this with a custom color palette of your choice.\n"+
													"; Just make sure it is named \"spectrum_width.pal\" and that you put it in this folder.\n"+
													"; If you want to make your own, make sure you consult:\n"+
													"; http://www.allisonhouse.com/grlevelx/manual/grlevel3/files_color_table.htm\n\n"+
													"units: KTS\n"+
													"step: 4\n\n"+
													"color: 0.0 4 142 222\n"+ 
													"color: 0.01 0 0 0 69 68 69\n"+
													"color: 4 79 77 78 176 162 165\n"+
													"color: 9.7 237 229 2 237 134 1\n"+
													"color: 13 237 115 1 161 3 0\n"+
													"color: 19 255 0 208 255 109 227\n"+
													"color: 25 255 136 232 255 218 246\n"+
													"color: 30 230 252 245 80 254 244\n"+
													"color: 40 80 254 244\n"+
													"RF: 103 0 158;\n";
	
	public static final String DEFAULT_1HR_RAIN_PAL = 	"; Default One Hour Rainfall Accumulation color palette.\n"+
														"; Feel free to replace this with a custom color palette of your choice.\n"+
														"; Just make sure it is named \"onehourrain.pal\" and that you put it in this folder.\n"+
														"; If you want to make your own, make sure you consult:\n"+
														"; http://www.allisonhouse.com/grlevelx/manual/grlevel3/files_color_table.htm\n\n"+
														"Units: Inches\n"+
														"Step:  1\n\n"+
														"SolidColor:  0.00    0  236  236\n"+
														"SolidColor:  0.10    1  160  246\n"+
														"SolidColor:  0.25    0    0  246\n"+
														"SolidColor:  0.50    0  255    0\n"+
														"SolidColor:  0.75    0  200    0\n"+
														"SolidColor:  1.00    0  144    0\n"+
														"SolidColor:  1.25  255  255    0\n"+
														"SolidColor:  1.50  231  192    0\n"+
														"SolidColor:  1.75  255  144    0\n"+
														"SolidColor:  2.00  255    0    0\n"+
														"SolidColor:  2.50  214    0    0\n"+
														"SolidColor:  3.00  192    0    0\n"+
														"SolidColor:  4.00  255    0  255\n"+
														"SolidColor:  6.00  153   85  201\n"+
														"SolidColor:  8.00  255  255  255\n";
	
	public static final String DEFAULT_STRM_TOTAL_PAL = "; Default Condition Red Precipitation color palette.\n"+
														"; Feel free to replace this with a custom color palette of your choice.\n"+
														"; Just make sure it is named \"stormtotal.pal\" and that you put it in this folder.\n"+
														"; If you want to make your own, make sure you consult:\n"+
														"; http://www.allisonhouse.com/grlevelx/manual/grlevel3/files_color_table.htm\n\n"+
														"Units: Inches\n"+
														"Step:  1\n\n"+
														"SolidColor:  0.00     0  236  236\n"+
														"SolidColor:  0.09     0  160  246\n"+
														"SolidColor:  0.19     0    0  246\n"+
														"SolidColor:  0.49     0  255    0\n"+
														"SolidColor:  0.74     0  200    0\n"+
														"SolidColor:  0.99     0  144    0\n"+
														"SolidColor:  1.49   255  255    0\n"+
														"SolidColor:  1.99   231  192    0\n"+
														"SolidColor:  2.49   255  144    0\n"+
														"SolidColor:  2.99   255    0    0\n"+
														"SolidColor:  3.99   214    0    0\n"+
														"SolidColor:  4.99   192    0    0\n"+
														"SolidColor:  5.99   255    0  255\n"+
														"SolidColor:  7.99   153   85  201\n"+
														"SolidColor:  9.99   255  255  255\n"+
														"SolidColor: 11.99   255  255  255";
	public ColorPalettes(){
		
	}
	
	public static void init(Palette pal){
		Palette.Type type = pal.getType();
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)){
			String file_path = Environment.getExternalStorageDirectory().toString() + DATA_DIR;
			if (!checkPalExists(file_path, type))
				if (!createDefaultPal(file_path, type)){
					//Log.e("ColoPalette.init()", "Error: Could not create palette file.");
					readDefaultPal(type, pal);
				}
				else{
					//Log.i("ColoPalette.init()", "Default color palette file successfully created!");
					readPalFile(file_path, type, pal);
				}
			else
				readPalFile(file_path, type, pal);
				
			pal.sort();
		}
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			String file_path = Environment.getExternalStorageDirectory().toString() + DATA_DIR;
			if (checkPalExists(file_path, type))
				readPalFile(file_path, type, pal);
			else
				readDefaultPal(type, pal);
				
			pal.sort();
		}
		else{
			readDefaultPal(type, pal);
			pal.sort();
		}
		
	}
	
	private static boolean createDefaultPal(String file_path, Palette.Type type){
		BufferedWriter writer;
		File file;
		File directory = new File(file_path);
		if (!directory.exists())
			directory.mkdir();
		switch(type){
		case REFL:
			file = new File(directory, REFL_PAL);
			try{
				file.createNewFile();
				FileWriter f_writer = new FileWriter(file);
				writer = new BufferedWriter(f_writer);
				writer.write(DEFAULT_REFL_PAL, 0, DEFAULT_REFL_PAL.length());
				writer.close();
				return true;
			}
			catch(FileNotFoundException e){
				return false;
			}
			catch (IOException ioe){
				return false;
			}
		case VEL:
			file = new File(directory, VEL_PAL);
			try{
				file.createNewFile();
				FileWriter f_writer = new FileWriter(file);
				writer = new BufferedWriter(f_writer);
				writer.write(DEFAULT_VEL_PAL, 0, DEFAULT_VEL_PAL.length());
				writer.close();
				return true;
			}
			catch(FileNotFoundException e){
				return false;
			}
			catch (IOException ioe){
				return false;
			}
		case VIL:
			file = new File(directory, VIL_PAL);
			try{
				file.createNewFile();
				FileWriter f_writer = new FileWriter(file);
				writer = new BufferedWriter(f_writer);
				writer.write(DEFAULT_VIL_PAL, 0, DEFAULT_VIL_PAL.length());
				writer.close();
				return true;
			}
			catch(FileNotFoundException e){
				return false;
			}
			catch (IOException ioe){
				return false;
			}
		case SW:
			file = new File(directory, SW_PAL);
			try{
				file.createNewFile();
				FileWriter f_writer = new FileWriter(file);
				writer = new BufferedWriter(f_writer);
				writer.write(DEFAULT_SW_PAL, 0, DEFAULT_SW_PAL.length());
				writer.close();
				return true;
			}
			catch(FileNotFoundException e){
				return false;
			}
			catch (IOException ioe){
				return false;
			}
		case ET:
			file = new File(directory, ET_PAL);
			try{
				file.createNewFile();
				FileWriter f_writer = new FileWriter(file);
				writer = new BufferedWriter(f_writer);
				writer.write(DEFAULT_ET_PAL, 0, DEFAULT_ET_PAL.length());
				writer.close();
				return true;
			}
			catch(FileNotFoundException e){
				return false;
			}
			catch (IOException ioe){
				return false;
			}
		case ONE_HR_RAIN:
			file = new File(directory, ONE_HR_PAL);
			try{
				file.createNewFile();
				FileWriter f_writer = new FileWriter(file);
				writer = new BufferedWriter(f_writer);
				writer.write(DEFAULT_1HR_RAIN_PAL, 0, DEFAULT_1HR_RAIN_PAL.length());
				writer.close();
				return true;
			}
			catch(FileNotFoundException e){
				return false;
			}
			catch (IOException ioe){
				return false;
			}
		case STRM_TOTAL:
			file = new File(directory, STRM_TOTAL_PAL);
			try{
				file.createNewFile();
				FileWriter f_writer = new FileWriter(file);
				writer = new BufferedWriter(f_writer);
				writer.write(DEFAULT_STRM_TOTAL_PAL, 0, DEFAULT_STRM_TOTAL_PAL.length());
				writer.close();
				return true;
			}
			catch(FileNotFoundException e){
				return false;
			}
			catch (IOException ioe){
				return false;
			}
		case SRV:
			file = new File(directory, SRV_PAL);
			try{
				file.createNewFile();
				FileWriter f_writer = new FileWriter(file);
				writer = new BufferedWriter(f_writer);
				writer.write(DEFAULT_SRV_PAL, 0, DEFAULT_SRV_PAL.length());
				writer.close();
				return true;
			}
			catch(FileNotFoundException e){
				return false;
			}
			catch (IOException ioe){
				return false;
			}
		default:
				return false;
		}
	}
	
	private static void readDefaultPal(Palette.Type type, Palette pal){
		BufferedReader reader;
		Pattern comment = Pattern.compile("(;.*)");
		Matcher comment_match;
		
		
		switch(type){
		case REFL:
			reader = new BufferedReader(new StringReader(DEFAULT_REFL_PAL));
			break;
		case VEL:
			reader = new BufferedReader(new StringReader(DEFAULT_VEL_PAL));
			break;
		case VIL:
			reader = new BufferedReader(new StringReader(DEFAULT_VIL_PAL));
			break;
		case SW:
			reader = new BufferedReader(new StringReader(DEFAULT_SW_PAL));
			break;
		case ET:
			reader = new BufferedReader(new StringReader(DEFAULT_ET_PAL));
			break;
		case ONE_HR_RAIN:
			reader = new BufferedReader(new StringReader(DEFAULT_1HR_RAIN_PAL));
			break;
		case STRM_TOTAL:
			reader = new BufferedReader(new StringReader(DEFAULT_STRM_TOTAL_PAL));
			break;
		case SRV:
			reader = new BufferedReader(new StringReader(DEFAULT_SRV_PAL));
			break;
		default:
			reader = new BufferedReader(new StringReader(DEFAULT_REFL_PAL));
			break;				
		}
		
		try{
			String line = reader.readLine();
			while (line != null){
				comment_match = comment.matcher(line);
				if (comment_match.find())
					line = comment_match.replaceAll("");
				
				readStep(type, line, pal);
				readColor(type, line, pal);
				readSolidColor(type, line, pal);
				readRFColor(type, line, pal);

				line = reader.readLine();
			}
			reader.close();
		}
		catch (IOException ie){
			//Log.e("ColorPalette.createDefaultPal()", "Uh oh, looks like can't read a pal file for some reason.");
			//ie.printStackTrace();
		}
	}
	
	private static void readPalFile(String file_path, Palette.Type type, Palette pal){
		BufferedReader reader;
		File file;
		Pattern comment = Pattern.compile("(;.*)");
		Matcher comment_match;
		
		
		switch(type){
		case REFL:
			file = new File(file_path + REFL_PAL);
			break;
		case VEL:
			file = new File(file_path + VEL_PAL);
			break;
		case VIL:
			file = new File(file_path + VIL_PAL);
			break;
		case SW:
			file = new File(file_path + SW_PAL);
			break;
		case ET:
			file = new File(file_path + ET_PAL);
			break;
		case ONE_HR_RAIN:
			file = new File(file_path + ONE_HR_PAL);
			break;
		case STRM_TOTAL:
			file = new File(file_path + STRM_TOTAL_PAL);
			break;
		case SRV:
			file = new File(file_path + SRV_PAL);
			break;
		default:
			file = new File(file_path + REFL_PAL);
			break;				
		}
		
		try{
			FileReader f_reader = new FileReader(file);
			reader = new BufferedReader(f_reader);
			String line = reader.readLine();
			while (line != null){
				comment_match = comment.matcher(line);
				if (comment_match.find())
					line = comment_match.replaceAll("");
				
				readUnits(type, line, pal);
				readColor(type, line, pal);
				readSolidColor(type, line, pal);
				readRFColor(type, line, pal);

				line = reader.readLine();
			}
			reader.close();
		}
		catch(FileNotFoundException e){
			//Log.e("ColorPalette.readPalFile()", "Uh oh, looks like can't read a pal file for some reason.", e);
		}
		catch (IOException ie){
			//Log.e("ColorPalette.readPalFile()", "Uh oh, looks like can't read a pal file for some reason.");
			//ie.printStackTrace();
		}
	}
	
	private static void readStep(Palette.Type type, String line, Palette pal){
		Pattern step = Pattern.compile("(\\bstep:)", Pattern.CASE_INSENSITIVE);
		Pattern number = Pattern.compile("(\\b[0-9]{1,3}\\b)");
		Matcher step_match;
		Matcher number_match;
		
		step_match = step.matcher(line);
		if (step_match.find()){
			number_match = number.matcher(line);
			if (number_match.find())
				pal.setStep(stringToInt(number_match.group()));
		}
	}
	
	
	private static void readColor(Palette.Type type, String line, Palette pal){
		Pattern color = Pattern.compile("(\\bcolor:)", Pattern.CASE_INSENSITIVE);
		Pattern number = Pattern.compile("((\\B-|\\b)?[0-9]{1,3}\\.?[0-9]*\\b)");
		Matcher color_match;
		Matcher number_match;
		
		color_match = color.matcher(line);
		if (color_match.find()){
			number_match = number.matcher(line);
			float value = Float.NaN;
			if (number_match.find())
				value = stringToFloat(number_match.group());
			int R = -1,G = -1,B = -1;
			int r = -1,g = -1,b = -1;
			if (number_match.find())
				R = stringToInt(number_match.group());	
			if (number_match.find())
				G = stringToInt(number_match.group());
			if (number_match.find())
				B = stringToInt(number_match.group());
			if (number_match.find())
				r = stringToInt(number_match.group());
			if (number_match.find())
				g = stringToInt(number_match.group());
			if (number_match.find())
				b = stringToInt(number_match.group());
			if ((R >= 0 && G >= 0 && B >= 0) && (r >= 0 && g >= 0 && b >= 0) && (value != Float.NaN)){
				pal.addColorStep(value, R, G, B, r, g, b);
				return;
			}
			if ((R >= 0 && G >= 0 && B >= 0) && (value != Float.NaN))
				pal.addColorStep(value, R, G, B, false);
		}		
	}
	
	private static void readSolidColor(Palette.Type type, String line, Palette pal){
		Pattern scolor = Pattern.compile("(\\bsolidcolor:)", Pattern.CASE_INSENSITIVE);
		Pattern number = Pattern.compile("((\\B-|\\b)?[0-9]{1,3}\\.?[0-9]*\\b)");
		Matcher scolor_match;
		Matcher number_match;
		
		scolor_match = scolor.matcher(line);
		if (scolor_match.find()){
			number_match = number.matcher(line);
			float value = Float.NaN;
			if (number_match.find())
				value = stringToFloat(number_match.group());
			int R = -1,G = -1,B = -1;
			if (number_match.find())
				R = stringToInt(number_match.group());	
			if (number_match.find())
				G = stringToInt(number_match.group());
			if (number_match.find())
				B = stringToInt(number_match.group());
			if ((R >= 0 && G >= 0 && B >= 0) && (value != Float.NaN))
				pal.addColorStep(value, R, G, B, true);
		}	
	}
	
	private static void readUnits(Palette.Type type, String line, Palette pal){
		Pattern units = Pattern.compile("(\\bunits:)", Pattern.CASE_INSENSITIVE);
		Pattern unit = Pattern.compile("(\\B|\\b)([a-z]{1,4})(/[a-z]{1,3}(2)?)?\\s*$");
		
		
		Matcher units_match;
		Matcher unit_match;
		
		units_match = units.matcher(line);
		if (units_match.find()){
			unit_match = unit.matcher(line);
			String value = "";
			if (unit_match.find()){
				value = unit_match.group();

				switch(type){
				case VEL:
				case SRV:
					Pattern kts = Pattern.compile("(kts|nmi/h(r)?)", Pattern.CASE_INSENSITIVE);
					Pattern mph = Pattern.compile("(mph|mi/h(r)?)", Pattern.CASE_INSENSITIVE);
					Pattern mps = Pattern.compile("m/s", Pattern.CASE_INSENSITIVE);
					Pattern kmph = Pattern.compile("km/h(r)?", Pattern.CASE_INSENSITIVE);
					
					unit_match = kts.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.KTS);
						return;
					}

					unit_match = mph.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.MPH);
						return;
					}

					unit_match = mps.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.MPS);
						return;
					}

					unit_match = kmph.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.KMPH);
						return;
					}
					
					pal.setUnits(Palette.Units.KTS);
					return;
				case REFL:
					pal.setUnits(Palette.Units.DBZ);
					return;
				case ONE_HR_RAIN:
				case STRM_TOTAL:
					Pattern in = Pattern.compile("in(ch)?", Pattern.CASE_INSENSITIVE);
					Pattern cm = Pattern.compile("cm", Pattern.CASE_INSENSITIVE);
					Pattern ft = Pattern.compile("ft", Pattern.CASE_INSENSITIVE);
					Pattern mm = Pattern.compile("mm", Pattern.CASE_INSENSITIVE);
					
					unit_match = in.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.IN);
						return;
					}

					unit_match = cm.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.CM);
						return;
					}
					
					unit_match = mm.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.MM);
						return;
					}

					unit_match = ft.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.FT);
						return;
					}
					
					pal.setUnits(Palette.Units.IN);
					return;
				case VIL:
					Pattern kgm2 = Pattern.compile("kg/m2", Pattern.CASE_INSENSITIVE);
					Pattern lbft2 = Pattern.compile("lb/ft2", Pattern.CASE_INSENSITIVE);
					
					unit_match = kgm2.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.KGM2);
						return;
					}
					
					unit_match = lbft2.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.LBFT2);
						return;
					}
					
					pal.setUnits(Palette.Units.KGM2);
					return;
				case ET:					
					Pattern kft = Pattern.compile("kft", Pattern.CASE_INSENSITIVE);
					Pattern km = Pattern.compile("km", Pattern.CASE_INSENSITIVE);
					Pattern mi = Pattern.compile("mi", Pattern.CASE_INSENSITIVE);
					
					unit_match = kft.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.KFT);
						return;
					}

					unit_match = km.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.KM);
						return;
					}

					unit_match = mi.matcher(value);
					if (unit_match.find()){
						pal.setUnits(Palette.Units.MI);
						return;
					}
					
					pal.setUnits(Palette.Units.KFT);
					return;
				}
			}
		}	
	}
	
	private static void readRFColor(Palette.Type type, String line, Palette pal){
		Pattern RF = Pattern.compile("(\\bRF:)", Pattern.CASE_INSENSITIVE);
		Pattern number = Pattern.compile("(\\b[0-9]{1,3}\\b)");
		Matcher RF_match;
		Matcher number_match;
		
		RF_match = RF.matcher(line);
		if (RF_match.find()){
			number_match = number.matcher(line);
			int R = -1,G = -1,B = -1;
			if (number_match.find())
				R = stringToInt(number_match.group());	
			if (number_match.find())
				G = stringToInt(number_match.group());
			if (number_match.find())
				B = stringToInt(number_match.group());
			if (R >= 0 && G >= 0 && B >= 0)
				pal.addRFColor(R, G, B);
		}	
	}
	
	private static boolean checkPalExists(String file_path, Palette.Type type){
		File pal;
		switch(type){
		case REFL:
			pal = new File(file_path + REFL_PAL);
			return pal.exists();
		case VEL:
			pal = new File(file_path + VEL_PAL);
			return pal.exists();
		case VIL:
			pal = new File(file_path + VIL_PAL);
			return pal.exists();
		case SW:
			pal = new File(file_path + SW_PAL);
			return pal.exists();
		case ET:
			pal = new File(file_path + ET_PAL);
			return pal.exists();
		case ONE_HR_RAIN:
			pal = new File(file_path + ONE_HR_PAL);
			return pal.exists();
		case STRM_TOTAL:
			pal = new File(file_path + STRM_TOTAL_PAL);
			return pal.exists();
		case SRV:
			pal = new File(file_path + SRV_PAL);
			return pal.exists();
		default:
			return false;
		}
	}
	
	/**
	 * Takes a string of a set of numbers, e.g. "4950", and turns
	 * it into an actual integer.
	 * @param _number  - String of the number you want.
	 * @return The integer value the characters in the string represent.  
	 * Returns -1 if the converstion failed.
	 */
	private static int stringToInt(String _number){
		int i = -1;
		
		// Try to get figure out what the numbers are in the string.
		try{
			i = Integer.parseInt(_number.trim());
	    }
	    catch (NumberFormatException nfe){
	    	float f = stringToFloat(_number.trim());
	    	// Uhhh... there isn't really an integer in that string.
	    	return (int)(f + 0.5f);
	    }
	    return i;
	}
	
	/**
	 * Takes a string of a set of numbers, e.g. "4950", and turns
	 * it into an actual integer.
	 * @param _number  - String of the number you want.
	 * @return The integer value the characters in the string represent.  
	 * Returns -1 if the converstion failed.
	 */
	private static float stringToFloat(String _number){
		float f = -1;
		
		// Try to get figure out what the numbers are in the string.
		try{
			f = Float.parseFloat(_number.trim());
	    }
	    catch (NumberFormatException nfe){
	    	// Uhhh... there isn't really an integer in that string.
	    	return -1;
	    }
	    return f;
	}
}