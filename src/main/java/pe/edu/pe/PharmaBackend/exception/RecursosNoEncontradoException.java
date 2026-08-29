package pe.edu.pe.PharmaBackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursosNoEncontradoException extends RuntimeException{
    public RecursosNoEncontradoException(String mensaje){
        super(mensaje);
    }
}
