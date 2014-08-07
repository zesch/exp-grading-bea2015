package de.unidue.langtech.grading.io;

public class PowerGradingItem
{
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(questionId);
        sb.append(" (");
        sb.append(grader1 + "/" + grader2 + "/" + grader3);
        sb.append(") ");
        String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
        sb.append(subStringText);
        sb.append(" ...");
        
        return sb.toString();        
    }

    private String studentId;
    private int questionId;
    private String text;
    private int grader1;
    private int grader2;
    private int grader3;
    
	public PowerGradingItem(String studentId, int questionId, String text,
			int grader1, int grader2, int grader3) {
		super();
		this.studentId = studentId;
		this.questionId = questionId;
		this.text = text;
		this.grader1 = grader1;
		this.grader2 = grader2;
		this.grader3 = grader3;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getGrader1() {
		return grader1;
	}

	public void setGrader1(int grader1) {
		this.grader1 = grader1;
	}

	public int getGrader2() {
		return grader2;
	}

	public void setGrader2(int grader2) {
		this.grader2 = grader2;
	}

	public int getGrader3() {
		return grader3;
	}

	public void setGrader3(int grader3) {
		this.grader3 = grader3;
	}
}