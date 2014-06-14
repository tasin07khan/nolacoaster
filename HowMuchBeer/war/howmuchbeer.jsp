<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.howmuchbeer.main.PMF" %>
<%@ page import="com.howmuchbeer.main.Comment" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="javax.jdo.Query" %>
<%@ page import="org.jsoup.Jsoup" %>
<%@ page import="org.jsoup.safety.Whitelist" %>

<html>
<head><title>How Much Beer?</title>
<link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-21951229-1']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>
</head>

<style type="text/css">
p.thanks
{
border-style: solid;
border-width: 2px;
border-color: #002200;
background-color: #aaeeaa;
color: #002200;
padding: 5px;
}

div.quote
{
padding-left:2em;
padding-right:4em;
}

input.small
{
width: 30px;
}
</style>

<script type="text/javascript">
function checkNumber(value, element) {
  if(value == "" || isNaN(value)) {
    alert(element + " must be a number.");
    return false;
  }
  return true;
}

function checkPositive(value, element) {
  if( value <= 0 ) {
    alert(element + " must be a positive number.");
    return false;
  }
  return true;
}

function validateBefore()
{
  var attendees = document.forms["beforeForm"]["attendees"].value;
  if(!checkNumber(attendees, "Attendees")) { return false; }
  if(!checkPositive(attendees, "Attendees")) { return false; }
}

function validateAfter()
{
  var attendees = document.forms["afterForm"]["attendees"].value;
  if(!checkNumber(attendees, "Attendees")) { return false; }
  if(!checkPositive(attendees, "Attendees")) { return false; }
  
  var quantity = document.forms["afterForm"]["quantity"].value;
  if(!checkNumber(quantity, "Quantity")) { return false; }
  if(!checkPositive(quantity, "Quantity")) { return false; }
}
</script>

<body>


<div style="padding:0;margin:0;background:#123456"> 
<div style="margin-left:15%;width:650px;background:#ABCDEF;border-left:2px solid #567890;border-right:10px solid #567890;border-bottom:5px solid #567890;padding:1em"> 
<center><img src="/images/howmuchbeerlogo.png"/></center>
<%
if ("thanks".equals(request.getParameter("result"))) {
%>
<p class="thanks">Thanks for your submission!
<%
} else if("captchafail".equals(request.getParameter("result"))) {
%>
<p class="thanks">Sorry. Your response to the captcha was incorrect.
<%
}
%>

<h1>Before the party...</h1>
<p>Find out how much beer to buy:

<form name="beforeForm" action="/result.jsp" method="get" onsubmit="return validateBefore()">
<div class="quote">
"I'm going to have <input class="small" type="text" name="attendees"/> people, and I think it's going to be a
<select name="craziness"><option value="NORMAL">normal</option>
<option value="CHILL">chill</option><option value="WILD">wild</option>
</select> party."</div>
<p><input type="submit" value="How much?" />
</form>

<hr>

<h1>After the party...</h1>
<p>Tell us how much you drank!

<form name="afterForm" action="/after" method="post" onsubmit="return validateAfter()">
<div class="quote">
<p>"It was a <select name="craziness"><option value="NORMAL">normal</option>
<option value="CHILL">chill</option><option value="WILD">wild</option>
</select> party. We had <input class="small" type="text" name="attendees"/> people show
up, and we drank <input class="small" type="text" name="quantity"/><select name="container">
<option value="CASE_24">cases (24)</option><option value="BOTTLE_12">bottles (12oz)</option>
<option value="CASE_30">cases (30)</option><option value="SIX_PACK">six packs</option>
<option value="TWELVE_PACK">twelve packs</option><option value="KEG">kegs</option>
<option value="PINT">pints</option>
<option value="FORTY">40s</option><option value="SIX_PACK_TALL">six packs (tall boys)</option>
<option value="BOTTLE_24">bottles (24oz)</option><option value="HALF_KEG">'half' kegs</option>
<option value="CORNY_KEG">corny kegs</option><option value="POWER_HOUR">power hours</option>
<option value="OUNCE">ounces</option><option value="GALLON">gallons</option>
</select> of beer!"</div>
<p><input type="submit" value="Tell Us" />
</form>
<center>
<%  UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    String login_string = userService.isUserLoggedIn() ? "Log Out (" + user.getNickname() + ")" : "Log In";
    String login_address = userService.isUserLoggedIn() ? 
        userService.createLogoutURL(request.getRequestURI()) : 
        userService.createLoginURL(request.getRequestURI()); %> 
<table style="font-size:0.8em;"><tr>
  <td><a href="<%=login_address%>"><%=login_string%></a></td>
  <td><a href="https://market.android.com/details?id=com.howmuchbeer.mobile">Download the Android App</a></td>
  <td><a href="faq.html">FAQ</a></td>
  <td><a href="http://www.google.com/recaptcha/mailhide/d?k=01mAXSiKYEFPKfG72kJrW5pg==&c=NCprYwPFZfHufsvwj7heYMOt2bR87buj5AcZSAUJmH8=">Contact</a></td>
</tr></table>
</center>

<hr>

<h1>Comments</h1>

<p>Leave a comment here.
<form name="commentForm" action="/recaptcha.jsp" method="post">
<p><textarea cols="50" rows="4" name="comment"></textarea>
<p>Name:<input style="text" value="Anonymous" name="author"></input><input type="submit" value="Leave a comment" />
</form>


<p>What others have said:
<% 
      PersistenceManager pm = PMF.get().getPersistenceManager();
  
    // Do query
    Query query = pm.newQuery(Comment.class);
    query.setOrdering("dateOfEntry desc");
    
    try {
      List<Comment> comments = (List<Comment>)query.execute();
  
      // Iterate, and output each book.
      for( Comment comment : comments ) {
        %>
        <p><small>"<%=Jsoup.clean(comment.getComment().getValue(), Whitelist.simpleText())%>," 
                   <i><%=Jsoup.clean(comment.getAuthor(), Whitelist.simpleText())%>, on <%=comment.getDateOfEntry()%></i></small>
        <%
      }
    } finally {
      query.closeAll();
    }
%>
<div class="clear"></div>
</div>
</div>
</body>
</html>