<table>
<tr>
<td>
<wiki:gadget border="0" width="275" height="390" url="http://jo4neo.googlecode.com/svn/trunk/jo4neo/misc/gadget3.xml"  /><br>
</td>
<td>
<h2>0.4.1 updates for latest neo4j api changes and full text indexing <a href='http://jo4neo.googlecode.com/svn/trunk/repo/thewebsemantic/jo4neo/0.4.1/'>here...</a></h2>
Simple object mapping for neo.  No byte code interweaving, just plain old reflection and plain old objects.<br>
<br>
<pre><code>public class Person {<br>
   //used by jo4neo<br>
   transient Nodeid node; <br>
   //simple property<br>
   @neo String firstName;<br>
   //helps you store a java.util.Date to neo4j <br>
   @neo Date date; <br>
   // jo4neo will index for you<br>
   @neo(index=true) String email; <br>
   // many to many relation<br>
   @neo Collection&lt;Role&gt; roles; <br>
<br>
   /* normal class oriented <br>
    * programming stuff goes here<br>
    */<br>
}<br>
</code></pre>
</td>
</tr>
</table>
## Annotating Java Object Properties ##
| @neo String name;|Persist me to the graph as a property|
|:-----------------|:------------------------------------|
| @neo Person friend;|Persist me to the graph as a node and relation|
| String misc;     |Don't bother to persist me           |
| @neo(index=true) String name;|Persist and index this field for me  |
| @neo(fulltext=true) String content;|Persist and full text index          |
| @neo("HAS\_JOB") Job job;|Persist using a given relation, by name|
| @embed Address address;|Persist as serialized byte array     |

## Locating Java Objects in the graph ##
If you ask jo4neo to index a property...
```
public class User {
 @neo(index=true) public String screenName;
...}
```
you may find it like this:
```
ObjectGraph graph = ...
User user = new User();
user = graph.find(user).where(user.screenName).is(screenName).result();
```

To find all nodes of a given type:

```
Collection<City> cities = graph.get(City.class);
```

To load a particular node as a java object:
```
Student s = graph.get(Student.class, 45);
```

To find the most recently added instances of a class:
```
// get the four latest roles added to the graph
Collection<Role> roles = graph.getMostRecent(Role.class, 4)
```