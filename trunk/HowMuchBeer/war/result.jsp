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

<html>
<head><title>How Much Beer? Results</title></head>
<style type="text/css">
div.one
{
width: 200px; height: 200px;
border-style: solid;
border-color: #444444;
background-color: #eeeeee;
padding: 2px;
}
</style>

<body>

<h1>Results</h1>

<%
    String craziness = request.getParameter("craziness");

    // Do query
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + BeerEventRecord.class.getName() + " where partyCraziness=='" + craziness + "'";
    List<BeerEventRecord> events = (List<BeerEventRecord>) pm.newQuery(query).execute();
    
    if (events.isEmpty()) {
%>
<p>Sorry! We have no data on parties that are <%=craziness%>.
<%
    } else {
      long average_oz = Calculations.mean(events);
      
      boolean was_long = true;
      Long attendees = Long.valueOf(0);
      try {
        attendees = Long.parseLong(request.getParameter("attendees"));
      } catch(NumberFormatException nfe) {
        was_long = false;
      }
      
      if (was_long) {
        // So, if you're in here, we are go for printing out shit.
        long std_dev = Calculations.stdDev(events);
        long mean_ounces = average_oz*attendees;
        long above_ounces = mean_ounces + std_dev;
        long below_ounces = mean_ounces - std_dev;
        below_ounces = below_ounces < 0 ? 0 : below_ounces;
%>
    <p>So you're having a <%=craziness%> party with <%=attendees%> people?

    <div class="one"><center>Average</center>
    <p>For an average party, you should buy:
      <ul>
      <%
        for(String s : new BasicAssortment().resultsForOunces(mean_ounces)) {
      %>
        <li><b><%=s%></b>
      <%
        } 
      %>
      </ul>
    </div>
    <%
      if(above_ounces != mean_ounces) { %>
        <div class="one"><center>More Beer (+1 STDEV)</center>
        <p>But if you want to be safe, you should buy:
          <ul>
          <%
            for(String s : new BasicAssortment().resultsForOunces(above_ounces)) {
          %>
            <li><b><%=s%></b>
          <%
            } 
          %>
          </ul>
        </div>
    <% } %>
    <% if(below_ounces != mean_ounces) { %>
      <div class="one"><center>Less Beer (-1 STDEV)</center>
      <p>And if you're trying to save money, buy:
        <ul>
        <%
          for(String s : new BasicAssortment().resultsForOunces(below_ounces)) {
        %>
          <li><b><%=s%></b>
        <%
          } 
        %>
        </ul>
      </div> <% }
    } else { %>
    <p> Uh oh, the number of attendees, <%=request.getParameter("attendees")%>, is not a number!
<%
      }
   }
%>

</body>
</html>