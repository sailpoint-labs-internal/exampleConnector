<?php
$con=mysqli_connect("127.0.0.1:3306","identityiq","identityiq","geo_table");
// Check connection
if (mysqli_connect_errno())
{
echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

$result = mysqli_query($con,"SELECT * FROM geo_table;");

echo "<table border='1'>
<tr>
<th>User Name</th>
<th>IP Address</th>
</tr>";

while($row = mysqli_fetch_array($result))
{
echo "<tr>";
echo "<td>" . $row['uname'] . "</td>";
echo "<td>" . $row['ip_online'] . "</td>";
echo "</tr>";
}
echo "</table>";

mysqli_close($con);
?><?php
