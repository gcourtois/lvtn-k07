//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;S_QUOTED_IDENTIFIER&gt;
 * </PRE>
 */
public class S_Quoted_Identifier extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;

   public S_Quoted_Identifier(NodeToken n0) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
   }

   public S_Quoted_Identifier() {}

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

