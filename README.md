<h1> Rest-api using VERTX, Mongo DB and JWT <h1>

<h3>Api with REST endpoints, such as :</h3>

<ul>
  <li>POST /register </li>
  <p>requirements : json object sent with request body with login(at leas 2 characters, no spaces) and password ( strong password, more info in Validate class)
</p><p>responses :
<ul>
 <li>204 Registering successful</li>
  <li>422 Please provide valid password and login </li>
  <li>409 User with this login exists</li>
 </ul></br>
  <li>POST /login </li>
  <p>requirements : json object sent with request body with login and password 
</p><p>responses :
<ul>
 <li>200 and json object with e.g.
 </br>token : eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiU3R1ZGVudCJ9.IxBkuQHrrwJrc8_IA5DPdGhBKx43iYsricXKXUQt_8o</li>
  <li>409 Login or password is incorrect</li>
</ul></br>

 <li>POST /item </li>
  <p>requirements : json object with item and authorization token in request header e.g. Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiU3R1ZGVudCJ9.IxBkuQHrrwJrc8_IA5DPdGhBKx43iYsricXKXUQt_8
</p><p>responses :
<ul>
 <li>204  Item created successfull</li>
  <li>401 You have not provided an authentication token, the one provided has expired, was revoked or is not authentic</li>
</ul></br>

<li>GET /item </li>
  <p>requirements : authorization token in request header
</p><p>responses :
<ul>
 <li>200  List of users item/s </li>
  <li>401 You have not provided an authentication token, the one provided has expired, was revoked or is not authentic</li>
</ul></br>

 <li>GET /all </li>
  <p>requirements :  none
</p><p>responses :
<ul>
 <li>200 and List of JsonObjects with all users
</ul></br>
</ul>


<h3> Building</h3>
Just compile and run MainVerticle.java , url : http://localhost:3000

<h3>Help</h3>

* https://vertx.io/docs/[Vert.x Documentation]
* https://stackoverflow.com/questions/tagged/vert.x?sort=newest&pageSize=15[Vert.x Stack Overflow]
* https://groups.google.com/forum/?fromgroups#!forum/vertx[Vert.x User Group]
* https://gitter.im/eclipse-vertx/vertx-users[Vert.x Gitter]

<p>This application was generated using http://start.vertx.io (dependencies and file structure)</p>


