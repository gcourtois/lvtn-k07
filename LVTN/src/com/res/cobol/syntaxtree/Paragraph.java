//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeChoice -> ( ParagraphName() | EntryStatement() )
 * nodeToken -> &lt;DOT&gt;
 * nodeChoice1 -> ( ExitProgramStatement() &lt;DOT&gt; | ExitStatement() &lt;DOT&gt; | AlteredGoto() | ( Sentence() )* )
 * </PRE>
 */
public class Paragraph extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeChoice nodeChoice;
   public NodeToken nodeToken;
   public NodeChoice nodeChoice1;

   public Paragraph(NodeChoice n0, NodeToken n1, NodeChoice n2) {
      nodeChoice = n0;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
      nodeToken = n1;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeChoice1 = n2;
      if ( nodeChoice1 != null ) nodeChoice1.setParent(this);
   }

   public Paragraph() {}

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

