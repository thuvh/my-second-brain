package org.brain2.test.search;

public class VietnameseIndex {
	public static class StringUtil
	{
	    private static final String[][] VietnameseSigns = new String[][]
	    {
	    	{"a","A","e","E","o","O","u","U","i","I","d","D","y","Y"},
	    	{"á","à","ạ","ả","ã","â","ấ","ầ","ậ","ẩ","ẫ","ă","ắ","ằ","ặ","ẳ","ẵ"},
	    	{"Á","À","Ạ","Ả","Ã","Â","Ấ","Ầ","Ậ","Ẩ","Ẫ","Ă","Ắ","Ằ","Ặ","Ẳ","Ẵ"},
	    	{"é","è","ẹ","ẻ","ẽ","ê","ế","ề","ệ","ể","ễ"},
	    	{"É","È","Ẹ","Ẻ","Ẽ","Ê","Ế","Ề","Ệ","Ể","Ễ"},
	    	{"ó","ò","ọ","ỏ","õ","ô","ố","ồ","ộ","ổ","ỗ","ơ","ớ","ờ","ợ","ở","ỡ"},
	    	{"Ó","Ò","Ọ","Ỏ","Õ","Ô","Ố","Ồ","Ộ","Ổ","Ỗ","Ơ","Ớ","Ờ","Ợ","Ở","Ỡ"},
	    	{"ú","ù","ụ","ủ","ũ","ư","ứ","ừ","ự","ử","ữ"},
	    	{"Ú","Ù","Ụ","Ủ","Ũ","Ư","Ứ","Ừ","Ự","Ử","Ữ"},
	    	{"í","ì","ị","ỉ","ĩ"},
	    	{"Í","Ì","Ị","Ỉ","Ĩ"},
	    	{"đ"},
	    	{"Đ"},
	    	{"ý","ỳ","ỵ","ỷ","ỹ"},
	    	{"Ý","Ỳ","Ỵ","Ỷ","Ỹ"}
	    };

	 
	    public static String RemoveSign4VietnameseString(String str) {
	        //Tiến hành thay thế , lọc bỏ dấu cho chuỗi
	        for (int i = 1; i < VietnameseSigns.length; i++) {
	            for (int j = 0; j < VietnameseSigns[i].length; j++){
	                str = str.replace(VietnameseSigns[i][j], VietnameseSigns[0][i - 1]);
	            }
	        }
	        return str;
	    }

	}
	
	public static void stringToCharacter(String str) {
		String s = "ÝỲỴỶỸ";
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		for (int i =0; i<s.length();i++) {
			ss.append("\"").append(Character.toString(s.charAt(i))).append("\"");
			if(i < s.length() - 1){
				ss.append(",");
			}
		}
		ss.append("}");
		System.out.println(ss.toString());
	}
	
	public static void main(String[] args) {
		String s = "Tiến hành thay thế , lọc bỏ dấu cho chuỗi";
		String clearedVietnamese = StringUtil.RemoveSign4VietnameseString(s);
		System.out.println(clearedVietnamese);
	}

	
	
}
