<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="user.UserDAO" %>

<%
	//test
	request.setCharacterEncoding("UTF-8");
	String id = request.getParameter("id");
	String pwd = request.getParameter("pwd");
	String type = request.getParameter("type");//로그인 요청인지 회원가입 요청인지를 구분하여 메서드를 실행하도록 합니다.
                                                
	
	//인스턴스 생성
	UserDAO User = new UserDAO(); 
	

	//회원가입
	if(type.equals("join")) {
		String returns = User.joindb(id, pwd);
		out.print(returns);
	} 
	//아이디 찾기
	else if(type.equals("forget-id")) {

	} 
	//비밀번호 찾기
	else if(type.equals("forget-pass")) {

	}
	
%>