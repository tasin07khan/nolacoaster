<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<html>
<head><title>How Much Beer?</title></head>

<style type="text/css">
p.thanks
{
border-style: solid;
border-width: 2px;
border-color: #002200;
background-color: #aaeeaa;
color: #002200;
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

<%
if ("thanks".equals(request.getParameter("result"))) {
%>
<p class="thanks">Thanks for submitting your party stats!
<%
}
%>

<h1>Before the party...</h1>
<p>Find out how much beer to buy:

<form name="beforeForm" action="/result.jsp" method="get" onsubmit="return validateBefore()">
"I'm going to have <input type="text" name="attendees"/> people, and I think it's going to be a
<select name="craziness"><option value="NORMAL">normal</option>
<option value="CHILL">chill</option><option value="wild">wild</option>
</select> party."
<p><input type="submit" value="How much?" />
</form>

<hr>

<h1>After the party...</h1>
<p>Tell us how much you drank!

<form name="afterForm" action="/after" method="post" onsubmit="return validateAfter()">
<p>"It was a <select name="craziness"><option value="NORMAL">normal</option>
<option value="CHILL">chill</option><option value="wild">wild</option>
</select> party. We had <input type="text" name="attendees"/> people show
up, and we drank <input type="text" name="quantity"/><select name="container">
<option value="CASE_24">cases (24)</option><option value="BOTTLE_12">bottles (12oz)</option>
<option value="CASE_30">cases (30)</option><option value="SIX_PACK">six packs</option>
<option value="TWELVE_PACK">twelve packs</option><option value="KEG">kegs</option>
<option value="PINT">pints</option>
<option value="FORTY">40s</option><option value="SIX_PACK_TALL">six packs (tall boys)</option>
<option value="BOTTLE_24">bottles (24oz)</option><option value="HALF_KEG">'half' kegs</option>
<option value="CORNY_KEG">corny kegs</option><option value="POWER_HOUR">power hours</option>
<option value="OUNCE">ounces</option><option value="GALLON">gallons</option>
</select> of beer!"
<p><input type="submit" value="Tell Us" />
</form>
</body>
</html>