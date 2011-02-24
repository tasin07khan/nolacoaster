<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.howmuchbeer.main.BeerEventRecord" %>
<%@ page import="com.howmuchbeer.main.Calculations" %>
<%@ page import="com.howmuchbeer.main.PMF" %>
<%@ page import="com.howmuchbeer.containers.BasicAssortment" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>

<!-- This page allows the admin to enter citations, which cary more weight, and also have a few extra fields. -->

<html>
<head><title>How Much Beer? Admin Panel</title>
<link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
</head>

<%
if ("thanks".equals(request.getParameter("result"))) {
%>
<p class="thanks">Thanks for submitting your party stats!
<%
}
%>

<%
if ("nothanks".equals(request.getParameter("result"))) {
%>
<p class="thanks">Submission did not work. Are you admin?
<%
}
%>


<% UserService userService = UserServiceFactory.getUserService();
   if(userService.isUserLoggedIn() && userService.isUserAdmin()) { %>
   
   <!-- form goes here -->
   <form action="/add_reference" method="post">
   
   <p><select name="craziness"><option value="NORMAL">normal</option>
      <option value="CHILL">chill</option><option value="WILD">wild</option></select> 
   <p>Attendees: <input type="text" name="attendees"/>
   <p>Quantity: <input type="text" name="quantity"/>
   <p><select name="container">
      <option value="CASE_24">cases (24)</option><option value="BOTTLE_12">bottles (12oz)</option>
      <option value="CASE_30">cases (30)</option><option value="SIX_PACK">six packs</option>
      <option value="TWELVE_PACK">twelve packs</option><option value="KEG">kegs</option>
      <option value="PINT">pints</option>
      <option value="FORTY">40s</option><option value="SIX_PACK_TALL">six packs (tall boys)</option>
      <option value="BOTTLE_24">bottles (24oz)</option><option value="HALF_KEG">'half' kegs</option>
      <option value="CORNY_KEG">corny kegs</option><option value="POWER_HOUR">power hours</option>
      <option value="OUNCE">ounces</option><option value="GALLON">gallons</option></select>
  <p>Citation: <input type="text" name="citation"/>
<p><input type="submit" value="Tell Us" />
   
   </form>
   
   <%
   } else if(!userService.isUserLoggedIn()) { 
     response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
   } else { %>
     <p>This page is for administrators only.
   <% } %>

</body>
</html>