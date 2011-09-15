//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;K_OPEN&gt;
 * relObjectName -> RelObjectName()
 * nodeOptional -> [ &lt;K_USING&gt; Arguments() ]
 * </PRE>
 */
public class SQLOpenStatement extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public RelObjectName relObjectName;
   public NodeOptional nodeOptional;

   public SQLOpenStatement(NodeToken n0, RelObjectName n1, NodeOptional n2) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      relObjectName = n1;
      if ( relObjectName != null ) relObjectName.setParent(this);
      nodeOptional = n2;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
   }

   public SQLOpenStatement() {}

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

