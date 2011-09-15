//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeChoice -> ( &lt;IDENTIFICATION&gt; | &lt;ID&gt; )
 * nodeToken -> &lt;DIVISION&gt;
 * nodeToken1 -> &lt;DOT&gt;
 * nestedProgramIdParagraph -> NestedProgramIdParagraph()
 * nodeListOptional -> ( IdentificationDivisionParagraph() )*
 * </PRE>
 */
public class NestedIdentificationDivision extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeChoice nodeChoice;
   public NodeToken nodeToken;
   public NodeToken nodeToken1;
   public NestedProgramIdParagraph nestedProgramIdParagraph;
   public NodeListOptional nodeListOptional;

   public NestedIdentificationDivision(NodeChoice n0, NodeToken n1, NodeToken n2, NestedProgramIdParagraph n3, NodeListOptional n4) {
      nodeChoice = n0;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
      nodeToken = n1;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeToken1 = n2;
      if ( nodeToken1 != null ) nodeToken1.setParent(this);
      nestedProgramIdParagraph = n3;
      if ( nestedProgramIdParagraph != null ) nestedProgramIdParagraph.setParent(this);
      nodeListOptional = n4;
      if ( nodeListOptional != null ) nodeListOptional.setParent(this);
   }

   public NestedIdentificationDivision() {}

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

