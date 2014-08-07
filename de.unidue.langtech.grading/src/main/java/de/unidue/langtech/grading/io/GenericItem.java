package de.unidue.langtech.grading.io;

public class GenericItem
{
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(studentId);
        sb.append(" (");
        sb.append(grade);
        sb.append(") ");
        String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
        sb.append(subStringText);
        sb.append(" ...");
        
        return sb.toString();        
    }

    private String studentId;
    private String text;
    private int grade;
    
	public GenericItem(String studentId, String text,
			int grade) {
		super();
		this.studentId = studentId;
		this.text = text;
		this.grade = grade;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}
}