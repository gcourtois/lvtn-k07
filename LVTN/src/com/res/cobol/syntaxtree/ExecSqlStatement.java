//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeChoice -> ( &lt;EXEC&gt; | &lt;EXECUTE&gt; )
 * nodeToken -> &lt;K_SQL&gt;
 * nodeChoice1 -> ( &lt;K_WHENEVER&gt; ( &lt;K_NOT&gt; &lt;K_FOUND&gt; | &lt;K_SQLERROR&gt; | &lt;K_SQLWARNING&gt; ) Statement() | ( ( SQLStatement() | DeclareCursorStatement() | &lt;K_PREPARE&gt; &lt;S_IDENTIFIER&gt; &lt;K_FROM&gt; &lt;S_BIND&gt; | &lt;K_ALTER&gt; &lt;K_SESSION&gt; SQLSetStatement() | &lt;K_EXECUTE&gt; SkipToEndExec() | &lt;K_CONNECT&gt; &lt;S_BIND&gt; | SkipToEndExec() ) &lt;END_EXEC&gt; ) )
 * </PRE>
 */
public class ExecSqlStatement extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeChoice nodeChoice;
   public NodeToken nodeToken;
   public NodeChoice nodeChoice1;

   public ExecSqlStatement(NodeChoice n0, NodeToken n1, NodeChoice n2) {
      nodeChoice = n0;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
      nodeToken = n1;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeChoice1 = n2;
      if ( nodeChoice1 != null ) nodeChoice1.setParent(this);
   }

   public ExecSqlStatement() {}

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

