package com.merlita.diariolab.Utils;


import static com.merlita.diariolab.Utils.Utils.archivoAOutputStream;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class FileHelper {

    private static final String TAG = "FileHelper";

    private final Context context;

    public FileHelper(Context context) {
        this.context = context;
    }

    public void copiarArchivoDeUri(Uri src, File dst) throws IOException {
        DocumentFile file = DocumentFile.fromSingleUri(context, src);

        try (FileOutputStream fos = new FileOutputStream(dst)){
            if (file != null && file.exists()) {
                // Abrir el InputStream
                InputStream is = context.getContentResolver().openInputStream(src);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }

                reader.close();
            } else {
                Log.e(TAG, "El archivo no existe");
            }
        }catch (Exception e){
            Log.e(TAG, "Error leyendo el archivo: "+e.getMessage());
        }

    }
    public boolean saveFile(Uri directoryUri, File fileName, String nombreArchivo) {
        try {
            DocumentFile folder = DocumentFile.fromTreeUri(context, directoryUri);

            if (folder == null || !folder.exists()) {
                return false; // La carpeta no existe o no se pudo acceder
            }

            DocumentFile file = folder.createFile("displayname", nombreArchivo);
            if (file == null) {
                return false; // No se pudo crear el archivo
            }

            // Escribir el contenido en el archivo
            OutputStream outputStream = context.getContentResolver().
                    openOutputStream(file.getUri());
            if (outputStream != null) {
                archivoAOutputStream(fileName, outputStream);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean copyFileToInternalStorage(Uri sourceUri, String destFileName) {
        try {
            // Obtener el nombre original del archivo
            String originalName = getFileName(sourceUri);
            if (originalName == null) originalName = "file";

            // Crear directorio interno si no existe
            File internalDir = context.getFilesDir();
            if (!internalDir.exists()) internalDir.mkdirs();

            // Crear archivo destino
            File destFile = new File(internalDir, destFileName);

            try (InputStream is = context.getContentResolver().openInputStream(sourceUri);
                 FileOutputStream fos = new FileOutputStream(destFile)) {

                if (is == null) {
                    return false;
                }else{
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    return true;
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "Error copying file: " + e.getMessage());
            return false;
        }
    }

    private String getFileName(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile != null) {
            return documentFile.getName();
        }
        return null;
    }
}