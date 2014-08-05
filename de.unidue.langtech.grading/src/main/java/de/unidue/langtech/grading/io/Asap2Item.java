package de.unidue.langtech.grading.io;

public class Asap2Item
{
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(textId);
        sb.append("-");
        sb.append(essaySetId);
        sb.append(" ");
        sb.append(goldClass);
        sb.append("(");
        sb.append(valClass);
        sb.append(") ");
        String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
        sb.append(subStringText);
        sb.append(" ...");
        
        return sb.toString();        
    }

    private int textId;
    private int essaySetId;
    private String goldClass;
    private String valClass;
    private String text;

    public Asap2Item(int textId, int essaySetId, String goldClass, String valClass, String text)
    {
        super();
        this.textId = textId;
        this.essaySetId = essaySetId;
        this.goldClass = goldClass;
        this.valClass = valClass;
        this.text = text;
    }

    public int getTextId()
    {
        return textId;
    }

    public void setTextId(int textId)
    {
        this.textId = textId;
    }

    public int getEssaySetId()
    {
        return essaySetId;
    }

    public void setEssaySetId(int essaySetId)
    {
        this.essaySetId = essaySetId;
    }

    public String getGoldClass()
    {
        return goldClass;
    }

    public void setGoldClass(String goldClass)
    {
        this.goldClass = goldClass;
    }

    public String getValClass()
    {
        return valClass;
    }

    public void setValClass(String valClass)
    {
        this.valClass = valClass;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
