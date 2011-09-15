//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;TALLYING&gt;
 * nodeList -> ( Identifier() &lt;FOR&gt; ( &lt;CHARACTERS&gt; ( BeforeAfterPhrase() )* | ( &lt;ALL&gt; | &lt;LEADING&gt; ) ( ( Identifier() | Literal() ) ( BeforeAfterPhrase() )* )+ )+ )+
 * nodeOptional -> [ ReplacingPhrase() ]
 * </PRE>
 */
public class TallyingPhrase extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public NodeList nodeList;
   public NodeOptional nodeOptional;

   public TallyingPhrase(NodeToken n0, NodeList n1, NodeOptional n2) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeList = n1;
      if ( nodeList != null ) nodeList.setParent(this);
      nodeOptional = n2;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
   }

   public TallyingPhrase() {}

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

