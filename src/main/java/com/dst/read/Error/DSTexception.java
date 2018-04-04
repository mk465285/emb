package com.dst.read.Error;
import java.util.*;

public class DSTexception extends Exception
{
	private List<Object> Err;
	private int ErrCode;
	
	{
		Err = new ArrayList<Object>();
	}
	
	public DSTexception(Object... ErrObj){
		for(int i = 0; i < ErrObj.length; i++){
			Err.add(ErrObj[i]);
		}
	}
	
	public DSTexception(String ErrStr){
		Err.add(ErrStr);
	}

	@Override
	public String getMessage()
	{
		StringBuffer Str = new StringBuffer("错误信息 : \n");
		for(int i = 0; i < Err.size(); i++){
			Str.append("	"+Err.get(i).toString());
		}
		if(null != super.getMessage()){
			Str.append("	"+super.getMessage());
		}
		return Str.toString();
	}
	public int getErrCode(){
	  return ErrCode;
	}
	
	public final static class Err_Type{
	  public static final int NOSTREAM = 0;
	  public static final int FILEERR = 1;
	  public static final int NOFILE = 2;
	  public static final int FILENODST =3;
	  public static final int NOTFILE = 4;
	  public static final int DATAREADERR = 5;
	  public static final int READHEADERERR = 6;
	}
	
	public static DSTexception Mythrow(String Errs,int mErrCode){
	  DSTexception T = new DSTexception(Errs);
	  T.ErrCode = mErrCode;
	  return T;
	}
	
}
