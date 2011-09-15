//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * relObjectName -> RelObjectName()
 * nodeOptional -> [ &lt;DOTCHAR&gt; DotObjectName() [ &lt;DOTCHAR&gt; DotObjectName() ] ]
 * nodeToken -> &lt;LPARENCHAR&gt;
 * nodeOptional1 -> [ [ &lt;K_DISTINCT&gt; | &lt;K_ALL&gt; ] ( SQLArguments() | &lt;ASTERISKCHAR&gt; ) ]
 * nodeToken1 -> &lt;RPARENCHAR&gt;
 * </PRE>
 */
public class FunctionCall extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public RelObjectName relObjectName;
   public NodeOptional nodeOptional;
   public NodeToken nodeToken;
   public NodeOptional nodeOptional1;
   public NodeToken nodeToken1;

   public FunctionCall(RelObjectName n0, NodeOptional n1, NodeToken n2, NodeOptional n3, NodeToken n4) {
      relObjectName = n0;
      if ( relObjectName != null ) relObjectName.setParent(this);
      nodeOptional = n1;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeToken = n2;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeOptional1 = n3;
      if ( nodeOptional1 != null ) nodeOptional1.setParent(this);
      nodeToken1 = n4;
      if ( nodeToken1 != null ) nodeToken1.setParent(this);
   }

   public FunctionCall() {}

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

