//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeChoice -> ( &lt;FD&gt; | &lt;SD&gt; )
 * fileName -> FileName()
 * nodeListOptional -> ( FileAndSortDescriptionEntryClause() )*
 * nodeToken -> &lt;DOT&gt;
 * </PRE>
 */
public class FileAndSortDescriptionEntry extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeChoice nodeChoice;
   public FileName fileName;
   public NodeListOptional nodeListOptional;
   public NodeToken nodeToken;

   public FileAndSortDescriptionEntry(NodeChoice n0, FileName n1, NodeListOptional n2, NodeToken n3) {
      nodeChoice = n0;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
      fileName = n1;
      if ( fileName != null ) fileName.setParent(this);
      nodeListOptional = n2;
      if ( nodeListOptional != null ) nodeListOptional.setParent(this);
      nodeToken = n3;
      if ( nodeToken != null ) nodeToken.setParent(this);
   }

   public FileAndSortDescriptionEntry() {}

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

