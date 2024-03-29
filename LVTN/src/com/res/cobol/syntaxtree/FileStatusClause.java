//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeOptional -> [ &lt;FILE&gt; ]
 * nodeToken -> &lt;STATUS&gt;
 * nodeOptional1 -> [ &lt;IS&gt; ]
 * qualifiedDataName -> QualifiedDataName()
 * nodeOptional2 -> [ QualifiedDataName() ]
 * </PRE>
 */
public class FileStatusClause extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeOptional nodeOptional;
   public NodeToken nodeToken;
   public NodeOptional nodeOptional1;
   public QualifiedDataName qualifiedDataName;
   public NodeOptional nodeOptional2;

   public FileStatusClause(NodeOptional n0, NodeToken n1, NodeOptional n2, QualifiedDataName n3, NodeOptional n4) {
      nodeOptional = n0;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeToken = n1;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeOptional1 = n2;
      if ( nodeOptional1 != null ) nodeOptional1.setParent(this);
      qualifiedDataName = n3;
      if ( qualifiedDataName != null ) qualifiedDataName.setParent(this);
      nodeOptional2 = n4;
      if ( nodeOptional2 != null ) nodeOptional2.setParent(this);
   }

   public FileStatusClause() {}

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

