//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;SEND&gt;
 * nodeChoice -> ( Identifier() | Literal() )
 * nodeOptional -> [ &lt;FROM&gt; Identifier() ]
 * nodeOptional1 -> [ &lt;WITH&gt; ( Identifier() | &lt;ESI&gt; | &lt;EMI&gt; | &lt;EGI&gt; ) ]
 * nodeOptional2 -> [ ( &lt;BEFORE&gt; | &lt;AFTER&gt; ) [ &lt;ADVANCING&gt; ] ( ( ( Identifier() | Literal() ) [ &lt;LINE&gt; | &lt;LINES&gt; ] ) | ( MnemonicName() | &lt;PAGE&gt; ) ) ]
 * </PRE>
 */
public class SendStatement extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public NodeChoice nodeChoice;
   public NodeOptional nodeOptional;
   public NodeOptional nodeOptional1;
   public NodeOptional nodeOptional2;

   public SendStatement(NodeToken n0, NodeChoice n1, NodeOptional n2, NodeOptional n3, NodeOptional n4) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeChoice = n1;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
      nodeOptional = n2;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeOptional1 = n3;
      if ( nodeOptional1 != null ) nodeOptional1.setParent(this);
      nodeOptional2 = n4;
      if ( nodeOptional2 != null ) nodeOptional2.setParent(this);
   }

   public SendStatement() {}

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

