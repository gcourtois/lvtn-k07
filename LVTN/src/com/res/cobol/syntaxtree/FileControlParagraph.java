//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeChoice -> ( &lt;FILE_CONTROL&gt; &lt;DOT&gt; | FileControlEntry() &lt;DOT&gt; )
 * nodeListOptional -> ( FileControlEntry() &lt;DOT&gt; )*
 * </PRE>
 */
public class FileControlParagraph extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeChoice nodeChoice;
   public NodeListOptional nodeListOptional;

   public FileControlParagraph(NodeChoice n0, NodeListOptional n1) {
      nodeChoice = n0;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
      nodeListOptional = n1;
      if ( nodeListOptional != null ) nodeListOptional.setParent(this);
   }

   public FileControlParagraph() {}

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

