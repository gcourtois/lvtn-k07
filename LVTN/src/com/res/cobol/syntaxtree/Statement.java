//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeChoice -> ( AcceptStatement() | AddStatement() | AlterStatement() | CallStatement() | CancelStatement() | CloseStatement() | ComputeStatement() | ContinueStatement() | DeleteStatement() | DisplayStatement() | DivideStatement() | EvaluateStatement() | ExitProgramStatement() | ExitStatement() | GobackStatement() | GotoStatement() | IfStatement() | InitializeStatement() | InspectStatement() | MergeStatement() | MoveStatement() | MultiplyStatement() | OpenStatement() | PerformStatement() | ReadStatement() | ReleaseStatement() | ReturnStatement() | RewriteStatement() | SearchStatement() | SetStatement() | SortStatement() | StartStatement() | StopStatement() | StringStatement() | SubtractStatement() | UnstringStatement() | WriteStatement() | ExecSqlStatement() | EnableStatement() | DisableStatement() | ReceiveStatement() | SendStatement() )
 * nodeOptional -> [ &lt;COMMACHAR&gt; ]
 * </PRE>
 */
public class Statement extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeChoice nodeChoice;
   public NodeOptional nodeOptional;

   public Statement(NodeChoice n0, NodeOptional n1) {
      nodeChoice = n0;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
      nodeOptional = n1;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
   }

   public Statement() {}

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

