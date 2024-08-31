package xyz.plumc.chattranslate.translates.ocr;

public enum OCR {
    LOCAL("local"),
    BAIDU("baidu"),
    BAIDU_PT("baidu_pt");

    public final String name;

    OCR(String name) {
        this.name = name;
    }

    public static OCR getOCR(String name){
        for(OCR ocr : OCR.values()){
            if(ocr.name.equals(name)){
                return ocr;
            }
        }
        return null;
    }
}
