//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeChoice -> ( &lt;AUTHOR&gt; | &lt;AUTHOR2&gt; )
 * nodeChoice1 -> ( &lt;DOT2&gt; | &lt;DOT&gt; )
 * nodeOptional -> [ CommentLine() ]
 * </PRE>
 */
public class AuthorParagraph extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeChoice nodeChoice;
   public NodeChoice nodeChoice1;
   public NodeOptional nodeOptional;

   public AuthorParagraph(NodeChoice n0, NodeChoice n1, NodeOptional n2) {
      nodeChoice = n0;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
      nodeChoice1 = n1;
      if ( nodeChoice1 != null ) nodeChoice1.setParent(this);
      nodeOptional = n2;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
   }

   public AuthorParagraph() {}

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

