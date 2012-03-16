package ll.mbr;

public class StudentDetail {

	private String loginState;
//	private String notFound;
	
	// student detail
	private String studentId;
	private String surname;
	private String firstname;
	private String gender;
	private String homeAddress;
	private String dateOfBirth;
	private String tel;
	private String mobile;
	private String email;
	private String course;
	private String courseCode;
	private String employmentRecord;
	
	public StudentDetail(){
		
	}
	
	public StudentDetail(String loginState){
		this.loginState = loginState;
	}
	
	public StudentDetail(String studentId,
			String surname,
			String firstname,
			String gender,
			String homeAddress,
			String dateOfBirth,
			String tel,
			String mobile,
			String email,
			String course,
			String courseCode,
			String employmentRecord){
		this.studentId = studentId;
		this.surname = surname;
		this.firstname = firstname;
		this.gender = gender;
		this.homeAddress = homeAddress;
		this.dateOfBirth = dateOfBirth;
		this.tel = tel;
		this.mobile = mobile;
		this.email = email;
		this.course = course;
		this.courseCode = courseCode;
		this.employmentRecord = employmentRecord;
	}	
	
	public void setLoginState(String loginState){
		this.loginState = loginState;
	}
	
	public String getLoginState(){
		return loginState;
	}		
	
//	public void setNotFound(String notFound){
//		this.notFound = notFound;
//	}
//	
//	public String getNotFound(){
//		return notFound;
//	}

	public void setId(String studentId){
		this.studentId = studentId;
	}
	
	public String getId(){
		return this.studentId;
	}
	
	public void setSurname(String surname){
		this.surname = surname;
	}
	
	public String getSurname(){
		return this.surname;
	}
	
	public void setFirstname(String firstname){
		this.firstname = firstname;
	}
	
	public String getFirstname(){
		return this.firstname;
	}
	
	public void setGender(String gender){
		this.gender = gender;
	}
	
	public String getGender(){
		return this.gender;
	}
	
	public void setHomeAddress(String homeAddress){
		this.homeAddress = homeAddress;
	}
	
	public String getHomeAddress(){
		return this.homeAddress;
	}
	
	public void setDateOfBirth(String dateOfBirth){
		this.dateOfBirth = dateOfBirth;
	}
	
	public String getDateOfBirth(){
		return this.dateOfBirth;
	}
	
	public void setTel(String tel){
		this.tel = tel;
	}
	
	public String getTel(){
		return this.tel;
	}
	
	public void setMobile(String mobile){
		this.mobile = mobile;
	}
	
	public String getMobile(){
		return this.mobile;
	}
		
	public void setEmail(String email){
		this.email = email;
	}
	
	public String getEmail(){
		return this.email;
	}
	
	public void setCourse(String course){
		this.course = course;
	}
	
	public String getCourse(){
		return this.course;
	}
	
	public void setCourseCode(String courseCode){
		this.courseCode = courseCode;
	}
	
	public String getCourseCode(){
		return this.courseCode;
	}
	
	public void setEmploymentRecord(String employmentRecord){
		this.employmentRecord = employmentRecord;
	}
	
	public String getEmploymentRecord(){
		return this.employmentRecord;
	}
}
