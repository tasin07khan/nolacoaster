<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>
<%@ page import="org.jsoup.Jsoup" %>
<%@ page import="org.jsoup.safety.Whitelist" %>

<html>
<head>
<title>Solve Captcha to Post Comment</title>
<link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
</head>
<body>
<div style="padding:0;margin:0;background:#123456"> 
<div style="margin-left:15%;width:650px;background:#ABCDEF;border-left:2px solid #567890;border-right:10px solid #567890;border-bottom:5px solid #567890;padding:1em">
<p>Solve the Captcha to post your comment:
  <form action="/postcomment" method="post">
  <%
    ReCaptcha c = ReCaptchaFactory.newReCaptcha("6LfSYskSAAAAAIuGvdY0bB-QflycMz7YRtbRzfkY", 
                                                "6LfSYskSAAAAAFcTmr4EOvNLB5r4CtgWJHQ80SbM", false);
    out.print(c.createRecaptchaHtml(null, null));
  %>
  <input type="hidden" name="author" value="<%=Jsoup.clean(request.getParameter("author"), Whitelist.simpleText())%>"></input>
  <input type="hidden" name="comment" value="<%=Jsoup.clean(request.getParameter("comment"), Whitelist.simpleText())%>"></input>
  <input type="submit" value="submit" />
  </form>
  </div>
  </div>
</body>
</html>