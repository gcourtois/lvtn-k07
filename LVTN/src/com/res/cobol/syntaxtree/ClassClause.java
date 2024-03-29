//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;CLASS&gt;
 * className -> ClassName()
 * nodeOptional -> [ &lt;IS&gt; ]
 * nodeList -> ( Literal() [ ( &lt;THROUGH&gt; | &lt;THRU&gt; ) Literal() ] )+
 * </PRE>
 */
public class ClassClause extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public ClassName className;
   public NodeOptional nodeOptional;
   public NodeList nodeList;

   public ClassClause(NodeToken n0, ClassName n1, NodeOptional n2, NodeList n3) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      className = n1;
      if ( className != null ) className.setParent(this);
      nodeOptional = n2;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeList = n3;
      if ( nodeList != null ) nodeList.setParent(this);
   }

   public ClassClause() {}

   public void accept(com.res.cobol.visitor.Visitor v) throws Exception {
       v.visit(this);
   }

   public <R, A> R accept(com.res.cobol.visitor.GJVisitor<R, A> v, A argu)
           throws Exception {
       return v.visit(this, argu);
   }

   public <R> R accept(com.res.cobol.visitor.GJNoArguVisitor<R> v)
           throws Exception {
       return v.visit(this);
   }

   public <A> void accept(com.res.cobol.visitor.GJVoidVisitor<A> v, A argu)
           throws Exception {
       v.visit(this, argu);
   }
   public void setParent(Node n) { parent = n; }
   public Node getParent()       { return parent; }
}

