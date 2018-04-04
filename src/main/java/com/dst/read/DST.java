package com.dst.read;

import android.app.*;
import android.os.*;
import java.io.*;
import com.dst.read.Error.*;
import com.dst.read.Listener.*;
import com.dst.read.DST.*;
import java.util.*;

public class DST 
{
  
  public final static class Data_Type{
	public final static int NORMAL = 0;
	public final static int JUMP = 1;
	public final static int CHANGECOLOR = 2;
	public final static int SLICE = 17;
	public final static int DOWN_SLICE_HEADE = 19;
	public final static int UP_SLICE_HEADER = 18;
  }

  private Open mConfig;

  private Encode_DST mEncode;

  {
	mEncode = new Encode_DST();
	mEncode.Header = new Header();
	mEncode.Data = new ArrayList<Dst_Body>();
  }


  public DST()
  {}
  public DST(Open config) throws DSTexception
  {
	buile(config);
	Read();
  }

  private void Read() throws DSTexception
  {
	synchronized ($Read_Dst.class)
	{
	  synchronized (mConfig)
	  {
		synchronized (mEncode)
		{
		  $Read_Dst.Encode(mConfig, mEncode);
		}
	  }
	}
  }
  public Header getHeader()
  {
	return mEncode.Header;
  }
  public List<DST.Dst_Body> getBody()
  {
	return mEncode.Data;
  }
  public DST buile(Open config) throws DSTexception
  {
	if (config.mInput == null){
	  throw DSTexception.Mythrow("打开错误",DSTexception.Err_Type.NOSTREAM);
	}
	this.mConfig = config;
	return this;
  }

  private static class Encode_DST
  {
	public Header Header;
	public List<Dst_Body> Data;
  }

  private final static class $Read_Dst
  {
	public static void Encode(final Open dst_config, final Encode_DST encode) throws DSTexception
	{
	  try
	  {
		encode.Header.setDesignName(ReadString(dst_config, 20).replace("LA:", "").trim());
		encode.Header.setStitchCount(ReadInt(dst_config, 11));
		encode.Header.setColorChangeCount(ReadInt(dst_config, 7));
		encode.Header.setXMax(ReadInt(dst_config, 9));
		encode.Header.setXMin(ReadInt(dst_config, 9));
		encode.Header.setYMax(ReadInt(dst_config, 9));
		encode.Header.setYMin(ReadInt(dst_config, 9));
		encode.Header.setXOffset(ReadInt(dst_config, 10));
		encode.Header.setYOffset(ReadInt(dst_config, 10));
		encode.Header.setMX(ReadInt(dst_config, 10));
		encode.Header.setMY(ReadInt(dst_config, 10));
		ReadString(dst_config, 11);
		encode.Data = ReadData(dst_config);
	  }
	  catch (Exception e)
	  {
		throw DSTexception.Mythrow("数据读取错误",DSTexception.Err_Type.READHEADERERR);
	  }
	}

	private static List<DST.Dst_Body> ReadData(DST.Open dst_config) throws IOException
	{
	  int index=0,color = 1;
	  int dX, dY, curAbsX = 0, curAbsY = 0;
	  boolean endFileFlag = false,isSlice=false;
	  List<DST.Dst_Body> Tmp = new ArrayList<DST.Dst_Body>();
	  dst_config.mInput.read(new byte[512 - (20 + 11 + 7 + (9 * 4) + (10 * 4) + 11 + 0)]);
	  while (endFileFlag != true)
	  {
		byte n[] = new byte[3];
		dst_config.mInput.read(n);
		Dst_Body info = new Dst_Body();
		int type = GetStitchType(n[2]);
		if (type != 0 && type != 1 && type != 2 && type != 3)
		{
		  endFileFlag = true;
		}
		else
		{
		  dX = decode_record_dx(n[0], n[1], n[2]);
		  dY = decode_record_dy(n[0], n[1], n[2]);
		  if (type == 0)
		  {
			curAbsX = curAbsX + dX;
			curAbsY = curAbsY + dY;
			info.setX(dX);
			info.setY(dY);
			info.setAbs_X(curAbsX);
			info.setAbs_Y(curAbsY);
			info.setNum(index);
			info.setType(Data_Type.NORMAL);
			info.setColor_Index(color);
			info.setJumpTag(false);
		  }
		  if (type == 1)
		  {
			curAbsX = curAbsX + dX;
			curAbsY = curAbsY + dY;
			info.setX(dX);
			info.setY(dY);
			info.setAbs_X(curAbsX);
			info.setAbs_Y(curAbsY);
			info.setNum(index);
			if(isSlice){
			  info.setType(Data_Type.SLICE);
			  info.setSlice(true);
			} else{
			info.setType(Data_Type.JUMP);
			  info.setJumpTag(true);
			}
			info.setColor_Index(color);
			
		  }
		  if (type == 2)
		  {
			color++;
			curAbsX = curAbsX + dX;
			curAbsY = curAbsY + dY;
			info.setX(dX);
			info.setY(dY);
			info.setAbs_X(curAbsX);
			info.setAbs_Y(curAbsY);
			info.setNum(index);
			info.setType(Data_Type.CHANGECOLOR);
			info.setColor_Index(color);
			info.setJumpTag(false);
		  }
		  if (type == 3)
		  {
			curAbsX = curAbsX + dX;
			curAbsY = curAbsY + dY;
			info.setX(dX);
			info.setY(dY);
			info.setAbs_X(curAbsX);
			info.setAbs_Y(curAbsY);
			info.setNum(index);
			info.setColor_Index(color);
			if(isSlice){
			  info.setType(Data_Type.UP_SLICE_HEADER);
			  isSlice = false;
			} else {
			  info.setType(Data_Type.DOWN_SLICE_HEADE);
			  isSlice = true;
			}
			info.setJumpTag(false);
		  }
		  if (dst_config.BodyListener != null)
		  {
			dst_config.BodyListener.Read(index, info.getType(), info.getX(), info.getY(), info.getColor_Index(), info.isJumpTag(), info.isSlice());
		  }
		  index++;
		  Tmp.add(info);
		}
	  }
	  return Tmp;
	}

	private static int GetStitchType(byte b2)
	{
	  if (b2 == 243) return 5;
	  switch (b2 & 0xC3)
	  {
		case 0x03: return Data_Type.NORMAL;
		case 0x83: return Data_Type.JUMP;
		case 0xC3: return Data_Type.CHANGECOLOR;
		case 0x43: return 3;
		default: return 4;
	  }
	}

	private static int getbit(byte b, int pos)
	{
	  int bit;
	  bit = (b >> pos) & 1;
	  return (bit);
	}

	private static int decode_record_dx(byte b0, byte b1, byte b2)
	{
	  int x = 0;
	  x += getbit(b2, 2) * (+81);
	  x += getbit(b2, 3) * (-81);
	  x += getbit(b1, 2) * (+27);
	  x += getbit(b1, 3) * (-27);
	  x += getbit(b0, 2) * (+9);
	  x += getbit(b0, 3) * (-9);
	  x += getbit(b1, 0) * (+3);
	  x += getbit(b1, 1) * (-3);
	  x += getbit(b0, 0) * (+1);
	  x += getbit(b0, 1) * (-1);
	  return (x);
	}

	private static int decode_record_dy(byte b0, byte b1, byte b2)
	{
	  int y = 0;
	  y += getbit(b2, 5) * (+81);
	  y += getbit(b2, 4) * (-81);
	  y += getbit(b1, 5) * (+27);
	  y += getbit(b1, 4) * (-27);
	  y += getbit(b0, 5) * (+9);
	  y += getbit(b0, 4) * (-9);
	  y += getbit(b1, 7) * (+3);
	  y += getbit(b1, 6) * (-3);
	  y += getbit(b0, 7) * (+1);
	  y += getbit(b0, 6) * (-1);
	  return (y);
	}

	private static int ReadInt(Open config, int len) throws Exception
	{
	  String Tmp = (ReadString(config, len, null)).split(":")[1];
	  return Integer.parseInt(Trim(Tmp));
	}

	private static String Trim(String tmp)
	{
	  for (int i=0;i < tmp.length();i++)
	  {}
	  return (tmp.replace(" ", "")).trim();
	}


	private static String ReadString(Open config, int len) throws Exception
	{
	  return ReadString(config, len, "GB2312");
	}

	private static String ReadString(Open config, int len, String chars) throws DSTexception
	{
	  byte Tmp[] = new byte[len];
	  try
	  {
		config.mInput.read(Tmp);
		if (chars == null)
		{
		  return new String(Tmp);
		}
		return new String(Tmp, chars);
	  }
	  catch (Exception e)
	  {
		throw DSTexception.Mythrow("数据读取错误",DSTexception.Err_Type.DATAREADERR);
	  }
	}
  }

  public static class Dst_Body
  {

	private int Num;
	private int X;
	private int Y;
	private int Abs_X;
	private int Abs_Y;
	private int Type;
	private int Color_Index;
	private boolean JumpTag;
	private boolean Slice;

	public void setSlice(boolean slice)
	{
	  Slice = slice;
	}

	public boolean isSlice()
	{
	  return Slice;
	}

	public void setJumpTag(boolean jumpTag)
	{
	  JumpTag = jumpTag;
	}

	public boolean isJumpTag()
	{
	  return JumpTag;
	}


	public void setNum(int num)
	{
	  Num = num;
	}

	public int getNum()
	{
	  return Num;
	}

	public void setX(int x)
	{
	  X = x;
	}

	public int getX()
	{
	  return X;
	}

	public void setY(int y)
	{
	  Y = y;
	}

	public int getY()
	{
	  return Y;
	}

	public void setAbs_X(int abs_X)
	{
	  Abs_X = abs_X;
	}

	public int getAbs_X()
	{
	  return Abs_X;
	}

	public void setAbs_Y(int abs_Y)
	{
	  Abs_Y = abs_Y;
	}

	public int getAbs_Y()
	{
	  return Abs_Y;
	}

	public void setType(int type)
	{
	  Type = type;
	}

	public int getType()
	{
	  return Type;
	}

	public void setColor_Index(int color_Index)
	{
	  Color_Index = color_Index;
	}

	public int getColor_Index()
	{
	  return Color_Index;
	}


  }

  public static class Header
  {
	private String designName;
	private int stitchCount;
	private int colorChangeCount;
	private int xMax;
	private int xMin;
	private int yMax;
	private int yMin;
	private int xOffset;
	private int yOffset;
	private int mX;
	private int mY;

	@Override
	public String toString()
	{
	  StringBuffer Tmp = new StringBuffer("designName = ");
	  Tmp.append(designName);
	  Tmp.append("\nstitchCount = ");
	  Tmp.append(stitchCount);
	  Tmp.append("\ncolorChangeCount = ");
	  Tmp.append(colorChangeCount);
	  Tmp.append("\nxMax = ");
	  Tmp.append(xMax);
	  Tmp.append("\nxMin = ");
	  Tmp.append(xMin);
	  Tmp.append("\nyMax = ");
	  Tmp.append(yMax);
	  Tmp.append("\nyMin = ");
	  Tmp.append(yMin);
	  Tmp.append("\nxOffset = ");
	  Tmp.append(xOffset);
	  Tmp.append("\nyOffset = ");
	  Tmp.append(yOffset);
	  Tmp.append("\nmX = ");
	  Tmp.append(mX);
	  Tmp.append("\nmY = ");
	  Tmp.append(mY);
	  return Tmp.toString();
	}



	public void setDesignName(String designName)
	{
	  this.designName = designName;
	}

	public String getDesignName()
	{
	  return designName;
	}

	public void setStitchCount(int stitchCount)
	{
	  this.stitchCount = stitchCount;
	}

	public int getStitchCount()
	{
	  return stitchCount;
	}

	public void setColorChangeCount(int colorChangeCount)
	{
	  this.colorChangeCount = colorChangeCount;
	}

	public int getColorChangeCount()
	{
	  return colorChangeCount;
	}

	public void setXMax(int xMax)
	{
	  this.xMax = xMax;
	}

	public int getXMax()
	{
	  return xMax;
	}

	public void setXMin(int xMin)
	{
	  this.xMin = xMin;
	}

	public int getXMin()
	{
	  return xMin;
	}

	public void setYMax(int yMax)
	{
	  this.yMax = yMax;
	}

	public int getYMax()
	{
	  return yMax;
	}

	public void setYMin(int yMin)
	{
	  this.yMin = yMin;
	}

	public int getYMin()
	{
	  return yMin;
	}

	public void setXOffset(int xOffset)
	{
	  this.xOffset = xOffset;
	}

	public int getXOffset()
	{
	  return xOffset;
	}

	public void setYOffset(int yOffset)
	{
	  this.yOffset = yOffset;
	}

	public int getYOffset()
	{
	  return yOffset;
	}

	public void setMX(int mX)
	{
	  this.mX = mX;
	}

	public int getMX()
	{
	  return mX;
	}

	public void setMY(int mY)
	{
	  this.mY = mY;
	}

	public int getMY()
	{
	  return mY;
	}}


  public final static class Open
  {
	public static String DstName = "dst";
	private InputStream mInput;
	
	private OnReadBodyListener BodyListener;
	
	public Open()
	{}
	public Open(String filePath) throws DSTexception
	{
	  this.mInput = ReadFilePath(filePath);
	}

	public Open Open(String filePath) throws DSTexception 
	{
	  return new Open(filePath);
	}

	public Open Open(File file) throws DSTexception
	{
	  return new Open(file);
	}

	public Open Open(InputStream input) throws DSTexception
	{
	  return new Open(input);
	}

	private InputStream ReadFilePath(String filePath) throws DSTexception
	{
	  File Tmp = new File(filePath);
	  if (Tmp.isDirectory())
	  {
		throw DSTexception.Mythrow("\"" + filePath + " \"不是一个文件!",DSTexception.Err_Type.NOTFILE);
	  }
	  else if (!Tmp.getPath().toLowerCase().endsWith(DstName.toLowerCase()))
	  {
		throw DSTexception.Mythrow("\"" + Tmp.getName() + " \"不是一个可以识别的文件!",DSTexception.Err_Type.FILENODST);
	  }
	  return ReadFile(Tmp);
	}
	public Open(File file) throws DSTexception
	{
	  this.mInput = ReadFile(file);
	}

	private InputStream ReadFile(File file) throws DSTexception
	{
	  if (!file.exists())
	  {
		throw DSTexception.Mythrow("\"" + file.getPath() + " \"不存在!",DSTexception.Err_Type.NOFILE);
	  }
	  try
	  {
		InputStream Tmp = new FileInputStream(file);
		return Tmp;
	  }
	  catch (FileNotFoundException e)
	  {
		throw DSTexception.Mythrow("\"" + file.getName() + "\" 打开错误",DSTexception.Err_Type.FILEERR);
	  }
	}
	public Open(InputStream input) throws DSTexception
	{
	  if (input == null)
	  {
		throw DSTexception.Mythrow("打开错误",DSTexception.Err_Type.NOSTREAM);
	  }
	  this.mInput = input;
	}
	public final DST Read() throws DSTexception
	{
	  return new DST(this);
	}
	public Open setOnReadBodyListener(OnReadBodyListener listener)
	{
	  this.BodyListener = listener;
	  return this;
	}
  }
}
