package com.msvusuarios.msvusuarios.controller;

import com.msvusuarios.msvusuarios.models.entity.Usuario;
import com.msvusuarios.msvusuarios.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class UsuarioController {
    @Autowired
    private UsuarioService service;

    @GetMapping("/listar")
    public List<Usuario>listar(){
        return service.listar();
    }

    @GetMapping("get/{id}")
    public ResponseEntity<?>detalle(@PathVariable Long id){
        Optional<Usuario> usuarioOptional=service.porId(id);
        if(usuarioOptional.isPresent()){
            return ResponseEntity.ok(usuarioOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/crear")
    public ResponseEntity<?>crear(@Valid  @RequestBody Usuario usuario, BindingResult result){
        if(result.hasErrors()){
            return validar(result);
        }
        if(service.porEmail(usuario.getEmail()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(Collections
                            .singletonMap("mensaje","Ya existe correo"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(usuario));
    }

    @PutMapping("editar/{id}")
    public ResponseEntity<?> editar(@Valid @RequestBody Usuario usuario, @PathVariable Long id, BindingResult result){
        if(result.hasErrors()){
            return validar(result);
        }

        Optional<Usuario> usuarioOptional=service.porId(id);
        if(usuarioOptional.isPresent()){
            Usuario usuarioDB= usuarioOptional.get();

            if(service.porEmail(usuario.getEmail()).isPresent()
            && !usuario.getEmail().equalsIgnoreCase(usuarioDB.getEmail())){
                return ResponseEntity.badRequest()
                        .body(Collections
                                .singletonMap("mensaje","Ya existe correo"));
            }

            usuarioDB.setNombre(usuario.getNombre());
            usuarioDB.setEmail(usuario.getEmail());
            usuarioDB.setPassword(usuario.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(usuarioDB));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("eliminar/{id}")
    public ResponseEntity<?>eliminar(@PathVariable Long id){
        Optional<Usuario> usuarioOptional=service.porId(id);
        if(usuarioOptional.isPresent()){
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<Map<String,String>> validar(BindingResult result){
        Map<String,String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errores.put(err.getField(),"El campo "+ err.getField()+" "+err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errores);
    }
}
