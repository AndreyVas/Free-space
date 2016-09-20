package application;

public class FileType 
{
	public static byte DIRECTORY = 0;
	public static byte FILE = 1;
	public static byte UNKNOWN = 2;
	
	private byte type;
	
	FileType()
	{
		this.type = UNKNOWN;
	}
	
	FileType(byte type)
	{
		this.type = type;
	}
	
	FileType(String t)
	{
		if(t.equals("FILE"))
			this.type = FILE;
		else if(t.equals("DIRECTORY"))
			this.type = DIRECTORY;
		else 
			this.type = UNKNOWN;
	}
	
	public boolean isDirectory()
	{
		if(type == DIRECTORY)
			return true;
		else
			return false;
	}
	
	public String getStringType()
	{
		if(isDirectory())
			return "DIRECTORY";
		else
			return "FILE";
	}
	
	public static String directory()
	{
		return "DIRECTORY";
	}
}
