package lox;

class RuntimeError extends RuntimeException {
    /** Added to fix the above error - 
     *  The serializable class RuntimeError does not declare a static final serialVersionUID field of type longJava(536871008)
     */
    private static final long serialVersionUID = -2866540382311160149L;
    final Token token;
  
    RuntimeError(Token token, String message) {
       super(message);
       this.token = token;
    }
}