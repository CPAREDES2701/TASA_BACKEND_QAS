package com.incloud.hcp.jco.tripulantes.service.impl;

import com.incloud.hcp.jco.tripulantes.dto.*;
import com.incloud.hcp.util.*;
import com.incloud.hcp.jco.tripulantes.service.JCOPDFsService;
import com.sap.conn.jco.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class JCOPDFsImpl implements JCOPDFsService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());



    public PDFExports GenerarPDFZarpe(PDFZarpeImports imports)throws Exception{

        PDFExports pdf=new PDFExports();
        PDFZarpeDto dto= new PDFZarpeDto();


        String path = Constantes.RUTA_ARCHIVO_IMPORTAR + "Archivo.pdf";
        try {

            JCoDestination destination = JCoDestinationManager.getDestination(Constantes.DESTINATION_NAME);
            JCoRepository repo = destination.getRepository();
            JCoFunction stfcConnection = repo.getFunction(Constantes.ZFL_RFC_REGZAR_ADM_REGZAR);

            JCoParameterList importx = stfcConnection.getImportParameterList();
            importx.setValue("P_TOPE", imports.getP_tope());
            importx.setValue("P_CDZAT", imports.getP_cdzat());
            importx.setValue("P_WERKS", imports.getP_werks());
            importx.setValue("P_WERKP", imports.getP_werkp());
            importx.setValue("P_CANTI", imports.getP_canti());
            importx.setValue("P_CDMMA", imports.getP_cdmma());
            importx.setValue("P_PERNR", imports.getP_pernr());

            JCoParameterList tables = stfcConnection.getTableParameterList();
            stfcConnection.execute(destination);

            JCoTable T_ZATRP = tables.getTable(Tablas.T_ZATRP);
            JCoTable T_DZATR = tables.getTable(Tablas.T_DZATR);
            JCoTable T_VGCER = tables.getTable(Tablas.T_VGCER);

            for(int i=0; i<T_ZATRP.getNumRows(); i++){
                T_ZATRP.setRow(i);


                String fechaArribo=ConvertirFecha(T_ZATRP, PDFZarpeConstantes.FEARR);
                String fecha=ConvertirFecha(T_ZATRP, PDFZarpeConstantes.FEZAT);
                String hora=ConvertirHora(T_ZATRP, PDFZarpeConstantes.HRARR);

                /*
                JCoField fieldH = T_ZATRP.getField(PDFZarpeConstantes.HRARR );
                Date time =fieldH.getTime();
                SimpleDateFormat hour = new SimpleDateFormat("HH:mm:ss");
                String hora = hour.format(time);*/

                String codigoZarpe=T_ZATRP.getString(PDFZarpeConstantes.CDZAT);
                int codigoZ=Integer.parseInt(codigoZarpe);
                String codigo =String.valueOf(codigoZ);
                dto.setCodigoZarpe(codigo);
                dto.setCapitania(T_ZATRP.getString(PDFZarpeConstantes.DSWKP));
                dto.setNombreNave(T_ZATRP.getString(PDFZarpeConstantes.DSWKS));
                dto.setMatricula(T_ZATRP.getString(PDFZarpeConstantes.MREMB));
                dto.setArqueoBruto(T_ZATRP.getString(PDFZarpeConstantes.AQBRT));
                dto.setZonaPesca(T_ZATRP.getString(PDFZarpeConstantes.DSWKP));
                dto.setTiempoOperacio(T_ZATRP.getString(PDFZarpeConstantes.TOPER));
                dto.setEstimadaArribo(fechaArribo+"   "+ hora);
                dto.setRepresentante(T_ZATRP.getString(PDFZarpeConstantes.RACRE ));
                dto.setEmergenciaNombre(T_ZATRP.getString(PDFZarpeConstantes.DSEMP));
                dto.setEmergenciaDireccion(T_ZATRP.getString(PDFZarpeConstantes.DFEMP));
                dto.setEmergenciaTelefono(T_ZATRP.getString(PDFZarpeConstantes.TFEMP));
                dto.setFecha(fecha);


            }
            logger.error("RolTripulacion");
            String[] CamposRolTripulacion= new String[]{PDFZarpeConstantes.NOMBR,
                                                        PDFZarpeConstantes.NRLIB,
                                                        PDFZarpeConstantes.FEFVG,
                                                        PDFZarpeConstantes.STEXT};
            String[][] RolTripulacion=new String[T_DZATR.getNumRows()+1][CamposRolTripulacion.length];

            RolTripulacion[0]= PDFZarpeConstantes.fieldRolTripulacion;
            int con=1;
            for(int i=0; i<T_DZATR.getNumRows(); i++){
                T_DZATR.setRow(i);

                String[] registros=new String[CamposRolTripulacion.length+1];
                int campos=0;
                for(int j=0; j<registros.length; j++){
                    if(j==0){
                        registros[j]=String.valueOf(con);

                    }else {
                            if(campos==2){

                                try {
                                    String fecha = ConvertirFecha(T_DZATR, PDFZarpeConstantes.FEFVG);
                                    registros[j] = fecha;
                                }catch (Exception e){
                                    registros[j] = T_DZATR.getString(CamposRolTripulacion[campos]);
                                }
                            }else
                            if(campos==3){
                                registros[j] = T_DZATR.getString(CamposRolTripulacion[campos]).replace("/","");
                            }else {
                                registros[j] = T_DZATR.getString(CamposRolTripulacion[campos]);
                            }
                            String dni = T_DZATR.getString(PDFZarpeConstantes.NRDNI);
                            if (registros[j].trim().compareTo("PATRON EP") == 0) {
                                dto.setNombrePatron(registros[1]);
                                dto.setDni(dni);
                            }

                        campos++;
                    }
                }

                RolTripulacion[con]=registros;
                con++;
            }
            logger.error("Certificados");

            String[] CamposCertificados= new String[]{PDFZarpeConstantes.DSCER,
                                                         PDFZarpeConstantes.FECCF};
            String[][] Certificados=new String[T_VGCER.getNumRows()+1][CamposCertificados.length];
            Certificados[0]= PDFZarpeConstantes.fieldCertificados;
            logger.error("Certificados_1");
            con=1;
            for(int i=0; i<T_VGCER.getNumRows(); i++){
                T_VGCER.setRow(i);

                String[] registros=new String[CamposCertificados.length+1];
                int campos=0;
                for(int j=0; j<registros.length; j++){

                    if(j==0){
                        registros[j]=String.valueOf(con);
                    }else if(j==1) {

                        registros[j] = T_VGCER.getString(CamposCertificados[campos]);
                        campos++;


                    }else if(j==2){
                        if(registros[1].trim().compareTo("ARQUEO")==0){

                            registros[j]=T_VGCER.getString(PDFZarpeConstantes.NRCER);

                        }else if(registros[1].trim().compareTo("REGISTRO DE RADIOBALIZA")==0
                                || registros[1].trim().compareTo("MATRICULA DE NAVES")==0
                                || registros[1].trim().compareTo("COMPENSACION DE COMPAS")==0) {

                            registros[j] = T_VGCER.getString(PDFZarpeConstantes.DSETP);
                        }else{
                            registros[j] = T_VGCER.getString(CamposCertificados[campos]);
                        }
                    campos++;
                    }
                }

                Certificados[con]=registros;
                con++;
            }


           PlantillaPDFZarpe(path, dto, RolTripulacion, Certificados);

            Metodos exec = new Metodos();
            pdf.setBase64(exec.ConvertirABase64(path));
            pdf.setMensaje("Ok");


        }catch (Exception e){
            pdf.setMensaje(e.getMessage());
        }
        return pdf;
    }

    public void PlantillaPDFZarpe(String path, PDFZarpeDto dto, String[][] rolTripulacion, String[][] certificados)throws Exception{

        logger.error("PlantillaPDF");

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);


        document.addPage(page);

        PDFont bold = PDType1Font.HELVETICA_BOLD;
        PDFont font = PDType1Font.HELVETICA;

        PDPageContentStream contentStream = new PDPageContentStream(document, page);


        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(170, 790);
        contentStream.drawString(PDFZarpeConstantes.titulo);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(230, 780);
        contentStream.drawString(PDFZarpeConstantes.titulo2);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(65, 765);
        contentStream.drawString(PDFZarpeConstantes.capitania );
        contentStream.endText();

        //insertando capitania guardacostas marítimas
        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(235, 765);
        contentStream.drawString(dto.getCapitania());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(235, 764);
        contentStream.drawString("___________________________________________________________________________");
        contentStream.endText();


        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 750);
        contentStream.drawString(PDFZarpeConstantes.uno);
        contentStream.endText();

        //insertando nombre de nave
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(150, 750);
        contentStream.drawString(dto.getNombreNave());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(150, 749);
        contentStream.drawString("_________________________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 740);
        contentStream.drawString(PDFZarpeConstantes.dos);
        contentStream.endText();

        //insertando numero de matrícula
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(150, 740);
        contentStream.drawString(dto.getMatricula());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(150, 739);
        contentStream.drawString("_______________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(300, 740);
        contentStream.drawString(PDFZarpeConstantes.tres);
        contentStream.endText();

        //insertando A.B.
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(334, 740);
        contentStream.drawString(dto.getArqueoBruto());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(334, 739);
        contentStream.drawString("__________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 730);
        contentStream.drawString(PDFZarpeConstantes.cuatro);
        contentStream.endText();

        //insertando zona de pesca
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(150, 730);
        contentStream.drawString(dto.getZonaPesca());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(150, 729);
        contentStream.drawString("_________________________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 720);
        contentStream.drawString(PDFZarpeConstantes.cinco);
        contentStream.endText();

        //insertar tiempo de operacion
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(180, 720);
        contentStream.drawString(dto.getTiempoOperacio());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(180, 719);
        contentStream.drawString("_____________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 710);
        contentStream.drawString(PDFZarpeConstantes.seis);
        contentStream.endText();

        //insertar dia y hora estimado de arribo
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(180, 710);
        contentStream.drawString(dto.getEstimadaArribo());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(180, 709);
        contentStream.drawString("_____________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 700);
        contentStream.drawString(PDFZarpeConstantes.siete);
        contentStream.endText();

        //insertando representante acreditado
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(180, 700);
        contentStream.drawString(dto.getRepresentante());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(180, 699);
        contentStream.drawString("_________________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 690);
        contentStream.drawString(PDFZarpeConstantes.ocho);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(165, 690);
        contentStream.drawString(PDFZarpeConstantes.ochoA);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(60, 680);
        contentStream.drawString(PDFZarpeConstantes.ochoB);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(60, 670);
        contentStream.drawString(PDFZarpeConstantes.ochoC);
        contentStream.endText();


        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 660);
        contentStream.drawString(PDFZarpeConstantes.nueve);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 360);
        contentStream.drawString(PDFZarpeConstantes.diez);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 228);
        contentStream.drawString(PDFZarpeConstantes.once);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(60, 216);
        contentStream.drawString(PDFZarpeConstantes.onceA);
        contentStream.endText();

        //Emergencia
        //insertando nombre
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(130, 216);
        contentStream.drawString(dto.getEmergenciaNombre());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(130, 215);
        contentStream.drawString("______________________________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(60, 204);
        contentStream.drawString(PDFZarpeConstantes.onceB);
        contentStream.endText();

        //insertando Dirección
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(130, 204);
        contentStream.drawString(dto.getEmergenciaDireccion());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(130, 203);
        contentStream.drawString("______________________________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(60, 192);
        contentStream.drawString(PDFZarpeConstantes.onceC);
        contentStream.endText();

        //insertando telefono
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(130, 192);
        contentStream.drawString(dto.getEmergenciaTelefono());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(130, 191);
        contentStream.drawString("______________________________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 180);
        contentStream.drawString(PDFZarpeConstantes.doce);
        contentStream.endText();

        //insertando nombre patron
        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(150, 180);
        contentStream.drawString(dto.getNombrePatron());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(150, 179);
        contentStream.drawString("_____________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(350, 180);
        contentStream.drawString(PDFZarpeConstantes.trece);
        contentStream.endText();

        //insertando fecha
        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(396, 180);
        contentStream.drawString(dto.getFecha());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(396, 179);
        contentStream.drawString("__________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 168);
        contentStream.drawString(PDFZarpeConstantes.catorce);
        contentStream.endText();

        //insertando dni
        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(150, 168);
        contentStream.drawString(dto.getDni());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(150, 167);
        contentStream.drawString("__________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(150, 125);
        contentStream.drawString(PDFZarpeConstantes.firma);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(175, 115);
        contentStream.drawString(PDFZarpeConstantes.firmaPatron);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 7);
        contentStream.moveTextPositionByAmount(350, 125);
        contentStream.drawString(PDFZarpeConstantes.firma);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(360, 115);
        contentStream.drawString(PDFZarpeConstantes.capitaniaGuardacosta);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 7);
        contentStream.moveTextPositionByAmount(50, 90);
        contentStream.drawString(PDFZarpeConstantes.nota);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(50, 80);
        contentStream.drawString(PDFZarpeConstantes.notaUno);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(50, 70);
        contentStream.drawString(PDFZarpeConstantes.notaDos);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(50, 60);
        contentStream.drawString(PDFZarpeConstantes.notaDos1);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(50, 50);
        contentStream.drawString(PDFZarpeConstantes.notaTres);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(50, 40);
        contentStream.drawString(PDFZarpeConstantes.notaTres1);
        contentStream.endText();

        logger.error("PlantillaPDF_1");
        drawTableRolTripulacion(page, contentStream, 655.0f, 50.0f, rolTripulacion);
        logger.error("PlantillaPDF_2");
        drawTableCertificados(contentStream,355, 50, certificados);

        contentStream.close();
        document.save(path);
        document.close();

    }

    public  void drawTableRolTripulacion(PDPage page, PDPageContentStream contentStream,
                                 float y, float margin, String[][] content) throws IOException {

        logger.error("drawTableRolTripulacion");
        final int rows = content.length;
        final int cols = content[0].length;
        final float rowHeight = 12.0f;
        final float tableWidth = page.getMediaBox().getWidth() - 2.0f * margin;
        final float tableHeight = rowHeight * (float) rows;
        final float colWidth = 85.33f;

        //draw the rows
        float nexty = y ;
        for (int i = 0; i <= rows; i++) {
                contentStream.moveTo(margin, nexty);
                contentStream.lineTo(margin + tableWidth, nexty);
                contentStream.stroke();
                nexty -= rowHeight;

        }


        //draw the columns
        float nextx = margin;
        for (int i = 0; i <= cols+1; i++) {

            if(i==1){
                nextx=margin+20;
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
            }else if(i==2){
                nextx=margin+209f;
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
            }else if(i==5){
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
                nextx += colWidth+30;
            }else {
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
                nextx += colWidth;
            }

        }


        //now add the text



        float texty=y-10;
        for(int i=0; i<content.length; i++) {

            String[]fields=content[i];
            float textx=margin+5;

            for (int j = 0; j < fields.length; j++) {

                switch (j) {
                    case 1:
                        if(i==0){
                            textx = 140;
                        }else {
                            textx = 90;
                        }
                        break;
                    case 2:
                        if(i==0){
                            textx=290;
                        }else {
                            textx = 280;
                        }
                        break;
                    case 3:
                        if(i==0){
                            textx = 380;
                        }else{
                            textx = 375;
                        }
                        break;
                    case 4:
                        if(i==0){
                            textx = 475;
                        }else {
                            textx = 460;
                        }
                        break;
                }

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 6);
                contentStream.newLineAtOffset(textx, texty);
                contentStream.showText(fields[j]);
                contentStream.endText();


            }
            texty-=12;
        }

    }

    public  void drawTableCertificados(PDPageContentStream contentStream,
                                         float y, float margin, String[][] content) throws IOException {

        logger.error("drawTableCertificados");
        final int rows = content.length;
        final int cols = content[0].length;
        final float rowHeight = 12.0f;
        final float tableWidth = 400.5f;
        final float tableHeight = rowHeight * (float) rows;
        //final float colWidth = tableWidth / (float) cols;
        final float colWidth = 170f;


        //draw the rows
        float nexty = y ;
        for (int i = 0; i <= rows; i++) {
            contentStream.moveTo(margin, nexty);
            contentStream.lineTo(margin + tableWidth, nexty);
            contentStream.stroke();
            nexty -= rowHeight;

        }


        //draw the columns
        float nextx = margin;
        for (int i = 0; i <= cols+1; i++) {

            if(i==1){
                nextx=margin+20;
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
            }else if(i==2){
                nextx=margin+230.2f;
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
            }else {
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
                nextx += colWidth;
            }
        }


        float texty=y-10;
        for(int i=0; i<content.length;i++) {

            String[]fields=content[i];
            float textx=margin+10;

            for (int j = 0; j < fields.length; j++) {

                switch (j) {
                    case 1:
                        if(i==0){
                            textx = 150;
                        }else {
                            textx = 100;
                        }
                        break;
                    case 2:
                        if(i==0){
                            textx = 340;
                        }else {
                            textx = 330;
                        }
                        break;

                }

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 6);
                contentStream.newLineAtOffset(textx, texty);
                contentStream.showText(fields[j]);
                contentStream.endText();


            }
            texty-=12;
        }


    }
    public void drawCuadroCodigoZarpe( PDPageContentStream contentStream, float y, float margin
                                        ,String codigoZarpe)throws IOException{

        final int rows = 1;
        final int cols = 1;
        final float rowHeight = 20.0f;
        final float tableWidth = 95f;
        final float tableHeight = 20;
        //final float colWidth = tableWidth / (float) cols;
        final float colWidth = 170f;
        //draw the rows
        float nexty = y ;
        for (int i = 0; i <= rows; i++) {
            contentStream.moveTo(margin, nexty);
            contentStream.lineTo(margin + tableWidth, nexty);
            contentStream.stroke();
            nexty -= rowHeight;

        }


        //draw the columns
        float nextx = margin;
        for (int i = 0; i <= cols; i++) {


                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
                nextx+=tableWidth;
        }

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
        contentStream.newLineAtOffset(margin+10, y-15);
        contentStream.showText("N°");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 11);
        contentStream.newLineAtOffset(margin+40, y-15);
        contentStream.showText(codigoZarpe);
        contentStream.endText();
    }

    public PDFExports GenerarPDFTravesia(PDFZarpeImports imports)throws Exception{

        String path = Constantes.RUTA_ARCHIVO_IMPORTAR + "Archivo.pdf";
        PDFExports pdf=new PDFExports();
        PDFTravesiaDto dto= new PDFTravesiaDto();

        try{

            JCoDestination destination = JCoDestinationManager.getDestination(Constantes.DESTINATION_NAME);
            JCoRepository repo = destination.getRepository();
            JCoFunction stfcConnection = repo.getFunction(Constantes.ZFL_RFC_REGZAR_ADM_REGZAR);

            JCoParameterList importx = stfcConnection.getImportParameterList();
            importx.setValue("P_TOPE", imports.getP_tope());
            importx.setValue("P_CDZAT", imports.getP_cdzat());
            importx.setValue("P_WERKS", imports.getP_werks());
            importx.setValue("P_WERKP", imports.getP_werkp());
            importx.setValue("P_CANTI", imports.getP_canti());
            importx.setValue("P_CDMMA", imports.getP_cdmma());
            importx.setValue("P_PERNR", imports.getP_pernr());

            JCoParameterList tables = stfcConnection.getTableParameterList();
            stfcConnection.execute(destination);

            JCoTable T_ZATRP = tables.getTable(Tablas.T_ZATRP);

            for(int i=0; i<T_ZATRP.getNumRows(); i++){
                T_ZATRP.setRow(i);

                JCoField fieldF = T_ZATRP.getField(PDFZarpeConstantes.FEARR);
                Date date=fieldF.getDate();
                SimpleDateFormat dia = new SimpleDateFormat("dd/MM/yyyy");
                String fechaArribo = dia.format(date);

                JCoField fieldH = T_ZATRP.getField(PDFZarpeConstantes.HRARR );
                Date time =fieldH.getTime();
                SimpleDateFormat hour = new SimpleDateFormat("HH:mm:ss");
                String horaArribo = hour.format(time);

                JCoField fieldFEZAT = T_ZATRP.getField(PDFZarpeConstantes.FEZAT);
                Date dateFEZAT=fieldFEZAT.getDate();
                SimpleDateFormat diaFEZAT = new SimpleDateFormat("dd/MM/yyyy");
                String fechaZarpe = diaFEZAT.format(dateFEZAT);

                JCoField fieldHRZAR = T_ZATRP.getField(PDFZarpeConstantes.HRZAR );
                Date timeHRZAR =fieldHRZAR.getTime();
                SimpleDateFormat hourHRZAR = new SimpleDateFormat("HH:mm:ss");
                String horaZarpe = hourHRZAR.format(timeHRZAR);


                dto.setNombreNave(T_ZATRP.getString(PDFZarpeConstantes.DSWKS));
                dto.setMatricula(T_ZATRP.getString(PDFZarpeConstantes.MREMB));
                dto.setZarpePuerto(T_ZATRP.getString(PDFZarpeConstantes.DSWKP));
                dto.setZpFecha(fechaZarpe);
                dto.setZpHora(horaZarpe);
                dto.setArriboPuerto(T_ZATRP.getString(PDFZarpeConstantes.DSWKP));
                dto.setApFecha(fechaArribo);
                dto.setApHora(horaArribo);
                dto.setZonaOperacion("");
                dto.setLatitud("");
                dto.setLongitud("");
                dto.setTiempo("");
                dto.setTiempoNavegacion("");
                dto.setTipoPesca("");
                dto.setCantidad("");
                dto.setDescargar("");
                dto.setComprador("");
                dto.setParteNovedadSalidaMar("");
                dto.setNombrePatron("");
                dto.setLugarFecha("");


                PlantillaPDFTravesia(path, dto);
                Metodos exec = new Metodos();
                pdf.setBase64(exec.ConvertirABase64(path));
                pdf.setMensaje("Ok");

            }

        }catch (Exception e){

            pdf.setMensaje(e.getMessage());
        }

        return pdf;
    }

    public void PlantillaPDFTravesia(String path, PDFTravesiaDto dto)throws Exception{

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        //String tasa= Constantes.RUTA_ARCHIVO_IMPORTAR+"logo.png";
        //String guardiaCostera= Constantes.RUTA_ARCHIVO_IMPORTAR+"logocapitania.png";
        //PDImageXObject logoTasa = PDImageXObject.createFromFile(tasa,document);
        //PDImageXObject logoGuardiaCostera = PDImageXObject.createFromFile(guardiaCostera,document);

        document.addPage(page);

// Create a new font object selecting one of the PDF base fonts
        PDFont bold = PDType1Font.HELVETICA_BOLD;
        PDFont font = PDType1Font.HELVETICA;

// Start a new content stream which will "hold" the to be created content
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        //logos superiores
       //contentStream.drawImage(logoTasa, 50, 750);
       // contentStream.drawImage(logoGuardiaCostera, 280, 750);

        contentStream.beginText();
        contentStream.setFont(bold, 10);
        contentStream.moveTextPositionByAmount(45, 710);
        contentStream.drawString(PDFTravesiaConstantes.titulo);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 670);
        contentStream.drawString(PDFTravesiaConstantes.uno);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(180, 670);
        contentStream.drawString(dto.getNombreNave());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(180, 670);
        contentStream.drawString("__________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 650);
        contentStream.drawString(PDFTravesiaConstantes.dos);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(180, 650);
        contentStream.drawString(dto.getMatricula());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(180, 650);
        contentStream.drawString("__________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 630);
        contentStream.drawString(PDFTravesiaConstantes.tres);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(170, 630);
        contentStream.drawString(dto.getZarpePuerto());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(170, 630);
        contentStream.drawString("____________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(300, 630);
        contentStream.drawString(PDFTravesiaConstantes.fecha);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(340, 630);
        contentStream.drawString(dto.getZpFecha());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(340, 630);
        contentStream.drawString("_________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(450, 630);
        contentStream.drawString(PDFTravesiaConstantes.hora);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(485, 630);
        contentStream.drawString(dto.getZpHora());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(485, 630);
        contentStream.drawString("___________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 610);
        contentStream.drawString(PDFTravesiaConstantes.cuatro);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(170, 610);
        contentStream.drawString(dto.getArriboPuerto());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(170, 610);
        contentStream.drawString("____________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(300, 610);
        contentStream.drawString(PDFTravesiaConstantes.fecha);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(340, 610);
        contentStream.drawString(dto.getApFecha());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(340, 610);
        contentStream.drawString("_________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(450, 610);
        contentStream.drawString(PDFTravesiaConstantes.hora);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(485, 610);
        contentStream.drawString(dto.getApHora());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(485, 610);
        contentStream.drawString("___________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 590);
        contentStream.drawString(PDFTravesiaConstantes.cinco);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(180, 590);
        contentStream.drawString(dto.getZonaOperacion());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(180, 590);
        contentStream.drawString("__________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(100, 570);
        contentStream.drawString(PDFTravesiaConstantes.latitud);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(150, 570);
        contentStream.drawString(dto.getLatitud());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(150, 570);
        contentStream.drawString("___________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(270, 570);
        contentStream.drawString(PDFTravesiaConstantes.longitud);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(330, 570);
        contentStream.drawString(dto.getLongitud());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(330, 570);
        contentStream.drawString("_______________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(420, 570);
        contentStream.drawString(PDFTravesiaConstantes.tiempo);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(465, 570);
        contentStream.drawString("_______________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(465, 570);
        contentStream.drawString(dto.getTiempo());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 550);
        contentStream.drawString(PDFTravesiaConstantes.seis);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(200, 550);
        contentStream.drawString(dto.getTiempoNavegacion());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(200, 550);
        contentStream.drawString("_______________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 530);
        contentStream.drawString(PDFTravesiaConstantes.siete);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(100, 510);
        contentStream.drawString(PDFTravesiaConstantes.sieteA);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(200, 510);
        contentStream.drawString(dto.getTipoPesca());
        contentStream.endText();

        int ySiete=510;
        int camposSiete=3;

        for(int i=0; i<camposSiete; i++){

            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.moveTextPositionByAmount(200, ySiete);
            contentStream.drawString("_______________________");
            contentStream.endText();
            ySiete-=20;
        }


        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(370, 510);
        contentStream.drawString(PDFTravesiaConstantes.sieteB);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(445, 510);
        contentStream.drawString(dto.getCantidad());
        contentStream.endText();

        ySiete=510;

        for(int i=0; i<camposSiete; i++){

            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.moveTextPositionByAmount(445, ySiete);
            contentStream.drawString("__________________");
            contentStream.endText();
            ySiete-=20;
        }


        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 430);
        contentStream.drawString(PDFTravesiaConstantes.ocho);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(150, 430);
        contentStream.drawString(dto.getDescargar());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(150, 430);
        contentStream.drawString("________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 410);
        contentStream.drawString(PDFTravesiaConstantes.nueve);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(150, 410);
        contentStream.drawString(dto.getComprador());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(150, 410);
        contentStream.drawString("________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 390);
        contentStream.drawString(PDFTravesiaConstantes.diez);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 370);
        contentStream.drawString(dto.getParteNovedadSalidaMar());
        contentStream.endText();

        int y=370;
        int campos=7;
        for(int i=0;i<campos; i++) {
            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.moveTextPositionByAmount(40, y);
            contentStream.drawString("____________________________________________________________________________________________");
            contentStream.endText();
            y-=20;
        }

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 230);
        contentStream.drawString(PDFTravesiaConstantes.once);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(250, 230);
        contentStream.drawString(dto.getNombrePatron());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(250, 230);
        contentStream.drawString("______________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(40, 210);
        contentStream.drawString(PDFTravesiaConstantes.doce);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(50, 180);
        contentStream.drawString("__________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(350, 210);
        contentStream.drawString(PDFTravesiaConstantes.trece);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(350, 180);
        contentStream.drawString(dto.getLugarFecha());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(350, 180);
        contentStream.drawString("__________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(40, 140);
        contentStream.drawString(PDFTravesiaConstantes.nota);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(40, 130);
        contentStream.drawString(PDFTravesiaConstantes.notaUno);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(45, 120);
        contentStream.drawString(PDFTravesiaConstantes.notaUnoA);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(40, 110);
        contentStream.drawString(PDFTravesiaConstantes.notaDos);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(40, 100);
        contentStream.drawString(PDFTravesiaConstantes.notatres);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(40, 90);
        contentStream.drawString(PDFTravesiaConstantes.notaCuatro);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(45, 80);
        contentStream.drawString(PDFTravesiaConstantes.notaCuatroA);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(45, 70);
        contentStream.drawString(PDFTravesiaConstantes.notaCuatroB);
        contentStream.endText();

        drawCuadroCodigoZarpe(contentStream, 780, 440,"PRUEBA");

        contentStream.close();
        document.save(path);
        document.close();
    }

    public PDFExports GenerarPDFProtestos(ProtestosImports imports)throws Exception{

        String path = Constantes.RUTA_ARCHIVO_IMPORTAR + "Archivo.pdf";
        PDFExports pdf= new PDFExports();
        PDFProtestosDto dto = new PDFProtestosDto();
        Metodos metodo=new Metodos();
        try {

            JCoDestination destination = JCoDestinationManager.getDestination(Constantes.DESTINATION_NAME);
            JCoRepository repo = destination.getRepository();
            JCoFunction stfcConnection = repo.getFunction(Constantes.ZFL_RFC_REGPRT_ADM_REGPRT);

            JCoParameterList importx = stfcConnection.getImportParameterList();
            importx.setValue("IP_TOPE", imports.getIp_tope());
            importx.setValue("IP_CDPRT", imports.getIp_cdprt());
            importx.setValue("IP_CANTI", imports.getIp_canti());
            importx.setValue("IP_PERNR", imports.getIp_pernr());

            List<Options> options = imports.getT_opcion();
            List<HashMap<String, Object>> tmpOptions = new ArrayList<HashMap<String, Object>>();
            for (int i = 0; i < options.size(); i++) {
                Options o = options.get(i);
                HashMap<String, Object> record = new HashMap<String, Object>();

                record.put("DATA", o.getData());
                tmpOptions.add(record);
            }

            JCoParameterList tables = stfcConnection.getTableParameterList();

            EjecutarRFC exec= new EjecutarRFC();
            exec.setTable(tables, Tablas.T_OPCION,tmpOptions);

            stfcConnection.execute(destination);

            JCoTable T_BAPRT = tables.getTable(Tablas.T_BAPRT);
            T_BAPRT.setRow(0);

            JCoField fieldF = T_BAPRT.getField(PDFProtestosConstantes.FECRE);
            Date date=fieldF.getDate();
            SimpleDateFormat dia = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es","ES"));
            String fecha = dia.format(date);

            dto.setTrato(T_BAPRT.getString(PDFProtestosConstantes.TRATO));
            dto.setGradoSupervisor(T_BAPRT.getString(PDFProtestosConstantes.GRADO));
            dto.setNombreSupervisor(T_BAPRT.getString(PDFProtestosConstantes.NAPSU));
            dto.setDomicilioLegal(T_BAPRT.getString(PDFProtestosConstantes.DRPTA));
            dto.setCargoSupervisor(T_BAPRT.getString(PDFProtestosConstantes.CARSU));
            dto.setNombreBahia(T_BAPRT.getString(PDFProtestosConstantes.NAPBA));
            dto.setDni(T_BAPRT.getString(PDFProtestosConstantes.NRDNI));
            dto.setPuerto(T_BAPRT.getString(PDFProtestosConstantes.DSWKP));
            dto.setFecha(fecha);
            dto.setNombreEmbarcacion(T_BAPRT.getString(PDFProtestosConstantes.DSWKS));
            dto.setMatricula(T_BAPRT.getString(PDFProtestosConstantes.MREMB));
            dto.setNumeroProtesto(T_BAPRT.getString(PDFProtestosConstantes.CDPRT));

            JCoTable T_TEXTOS = tables.getTable(Tablas.T_TEXTOS);

            String tdline="";
            for(int i=0; i<T_TEXTOS.getNumRows(); i++){
                T_TEXTOS.setRow(i);
                tdline+= T_TEXTOS.getString(PDFProtestosConstantes.TDLINE);
            }

            dto.setSegundoParrafo(tdline);


            PlantillaPDFProtestos(path, dto);

            pdf.setBase64(metodo.ConvertirABase64(path));

            pdf.setMensaje("Ok");

        }catch (Exception e){
            pdf.setMensaje(e.getMessage());
        }


        return pdf;
    }

    public void PlantillaPDFProtestos(String Path, PDFProtestosDto dto)throws Exception{

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);

        document.addPage(page);

        PDFont bold = PDType1Font.HELVETICA_BOLD;
        PDFont font = PDType1Font.HELVETICA;

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.beginText();
        contentStream.setFont(bold, 14);
        contentStream.moveTextPositionByAmount(190, 750);
        contentStream.drawString(PDFProtestosConstantes.titulo);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(365, 700);
        contentStream.drawString(dto.getPuerto()+", "+dto.getFecha());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(40, 670);
        contentStream.drawString(dto.getTrato());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(40, 660);
        contentStream.drawString(dto.getGradoSupervisor());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(40, 650);
        contentStream.drawString(dto.getNombreSupervisor());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(40, 640);
        contentStream.drawString(dto.getCargoSupervisor());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(40, 630);
        contentStream.drawString(PDFProtestosConstantes.capitaniaGuardacostas);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(40, 620);
        contentStream.drawString(dto.getPuerto());
        contentStream.endText();

        String parrafoUno=PDFProtestosConstantes.primerParrafo1+dto.getDomicilioLegal()+PDFProtestosConstantes.primerParrafo2+dto.getNombreBahia()
                +PDFProtestosConstantes.primerParrafo3+dto.getDni()+PDFProtestosConstantes.primerParrafo4+dto.getNombreEmbarcacion()
                +PDFProtestosConstantes.primerParrafo5+dto.getMatricula()+PDFProtestosConstantes.primerParrafo6;

        int finPU= justificarParrafoUno(contentStream,font,12f,  parrafoUno, 590);
        int finUno= justificarParrafo(contentStream,font,12f,  dto.getSegundoParrafo(), finPU-20);
        int finDos= justificarParrafo(contentStream,font,12f,  PDFProtestosConstantes.textoFinal1, finUno-20);


        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(40, finDos-20);
        contentStream.drawString(PDFProtestosConstantes.textoFinal2);
        contentStream.endText();



        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(40, finDos-60);
        contentStream.drawString(PDFProtestosConstantes.atentamente);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 9);
        contentStream.moveTextPositionByAmount(40, 90);
        contentStream.drawString(PDFProtestosConstantes.nProtesto+ dto.getNumeroProtesto());
        contentStream.endText();



        contentStream.close();
        document.save(Path);
        document.close();


    }

    public int justificarParrafoUno(PDPageContentStream contentStream, PDFont pdfFont, float fontSize,  String text, int startY)
            throws IOException{

        logger.error("justificarTexto");
        int width = 470;
        int startX = 40;


        List<String> lines = new ArrayList<String>();

        int lastSpace = -1;
        int con=0;
        int c=0;
        while (text.length()> 0)
        {

            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);

            float size = fontSize * pdfFont.getStringWidth(subString) / 1000;

            if (size > width)
            {
                if (lastSpace < 0)
                    lastSpace = spaceIndex;
                subString = text.substring(0, lastSpace);
                lines.add(subString);


                text = text.substring(lastSpace).trim();

                lastSpace = -1;
                logger.error("width antes: "+width);
                if(!lines.isEmpty()){

                    width = 510;
                    logger.error("width dsps: "+width);
                }
                //logger.error("contador: "+c);
                //logger.error("lines.lenght: "+lines.size());

                //logger.error("primer if("+con+"): "+lines.get(con));
                con++;
            }
            else if (spaceIndex == text.length())
            {

                lines.add(text);

                text = "";

            }
            else
            {
                lastSpace = spaceIndex;
            }

            c++;

        }


           for(int i=0; i<lines.size(); i++)
        {


            contentStream.beginText();
            contentStream.setFont(pdfFont, fontSize);
            if(i==0){
                contentStream.newLineAtOffset(startX+30, startY);
            }else {
                contentStream.newLineAtOffset(startX, startY);
            }

            float charSpacing = 0;
            float size=0.0f;
            if (lines.get(i).length() > 1)
            {
                 size = fontSize * pdfFont.getStringWidth(lines.get(i)) / 1000;
                if(i==0){
                    width=477;
                }else{
                    width=510;
                }
                float  free = width - size;
                int tam=lines.size()-1;
                if(i==tam){
                    charSpacing=0;
                }else {

                    if (free > 0) {
                        charSpacing = free / (lines.get(i).length() - 1);
                    }
                }
            }

            contentStream.setCharacterSpacing(charSpacing);
            contentStream.showText(lines.get(i));
            contentStream.endText();
            startY-=fontSize;

        }
        return startY;

    }

    public int justificarParrafo(PDPageContentStream contentStream, PDFont pdfFont, float fontSize,  String text, int startY)
            throws IOException{


        logger.error("justificarTexto");
        //float width = mediabox.getWidth() - 2*margin;
        //float startX = mediabox.getLowerLeftX() + margin;
        //float startY = mediabox.getUpperRightY() - margin;
        float width = 470;
        float startX = 40f;
        //float startY = 600;



        List<String> lines = new ArrayList<String>();


        int lastSpace = -1;
        while (text.length()> 0)
        {
            if(lines.size()>0){
                width=510;
            }
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            logger.error("subString: "+subString);
            float size = fontSize * pdfFont.getStringWidth(subString) / 1000;

            if (size > width)
            {
                if (lastSpace < 0)
                    lastSpace = spaceIndex;
                subString = text.substring(0, lastSpace);
                lines.add(subString);

                text = text.substring(lastSpace).trim();

                lastSpace = -1;
            }
            else if (spaceIndex == text.length())
            {
                logger.error("lines.add(text) "+text);
                lines.add(text);

                text = "";
            }
            else
            {
                lastSpace = spaceIndex;
            }
        }


        for(int i=0; i<lines.size(); i++)
        {
            contentStream.beginText();
            contentStream.setFont(pdfFont, fontSize);
            contentStream.newLineAtOffset(startX, startY);

            float charSpacing = 0;
            float size=0.0f;
            if (lines.get(i).length() > 1)
            {
                size = fontSize * pdfFont.getStringWidth(lines.get(i)) / 1000;

                logger.error("size "+size);
                float  free = width - size;
                int tam=lines.size()-1;
                if(i==tam){
                    charSpacing=0;
                }else {

                    if (free > 0) {
                        charSpacing = free / (lines.get(i).length() - 1);
                    }
                }
            }
            logger.error("charSpacing "+charSpacing);
            contentStream.setCharacterSpacing(charSpacing);
            contentStream.showText(lines.get(i));
            //contentStream.newLineAtOffset(startX, -leading);
            contentStream.endText();
            startY-=fontSize+2;

        }
        return startY;
    }

    public PDFExports GenerarPDFZarpeTravesia(PDFZarpeImports imports)throws Exception{
        PDFExports pdf = new PDFExports();
        String path = Constantes.RUTA_ARCHIVO_IMPORTAR + "Archivo.pdf";
        PDFZarpeTravesiaDto dto= new PDFZarpeTravesiaDto();

        try {

            JCoDestination destination = JCoDestinationManager.getDestination(Constantes.DESTINATION_NAME);
            JCoRepository repo = destination.getRepository();
            JCoFunction stfcConnection = repo.getFunction(Constantes.ZFL_RFC_REGZAR_ADM_REGZAR);

            JCoParameterList importx = stfcConnection.getImportParameterList();
            importx.setValue("P_TOPE", imports.getP_tope());
            importx.setValue("P_CDZAT", imports.getP_cdzat());
            importx.setValue("P_WERKS", imports.getP_werks());
            importx.setValue("P_WERKP", imports.getP_werkp());
            importx.setValue("P_CANTI", imports.getP_canti());
            importx.setValue("P_CDMMA", imports.getP_cdmma());
            importx.setValue("P_PERNR", imports.getP_pernr());

            JCoParameterList tables = stfcConnection.getTableParameterList();
            stfcConnection.execute(destination);

            JCoTable T_ZATRP = tables.getTable(Tablas.T_ZATRP);
            JCoTable T_DZATR = tables.getTable(Tablas.T_DZATR);
            JCoTable T_VGCER = tables.getTable(Tablas.T_VGCER);

            for(int i=0; i<T_ZATRP.getNumRows(); i++){
                T_ZATRP.setRow(i);

                String fechaArribo=ConvertirFecha(T_ZATRP, PDFZarpeConstantes.FEARR);
                String fechaZarpe=ConvertirFecha(T_ZATRP, PDFZarpeConstantes.FEZAT);
                String horaArribo=ConvertirHora(T_ZATRP, PDFZarpeConstantes.HRARR);
                String horaZarpe=ConvertirHora(T_ZATRP, PDFZarpeConstantes.HRZAR);


                dto.setCapitaniaGuardacostas(T_ZATRP.getString(PDFZarpeConstantes.DSWKP));
                dto.setNombreNave(T_ZATRP.getString(PDFZarpeConstantes.DSWKS));
                dto.setMatricula(T_ZATRP.getString(PDFZarpeConstantes.MREMB));
                dto.setArqueoBruto(T_ZATRP.getString(PDFZarpeConstantes.AQBRT));
                dto.setColorCasco(T_ZATRP.getString(PDFZarpeConstantes.COCAS));
                dto.setSuperEstructura(T_ZATRP.getString(PDFZarpeConstantes.COSUP));
                dto.setPropietario(T_ZATRP.getString(PDFZarpeConstantes.DSEMP));
                dto.setDomicilioFiscal(T_ZATRP.getString(PDFZarpeConstantes.DFEMP));
                dto.setRepresentanteAcreditado(T_ZATRP.getString(PDFZarpeConstantes.RACRE ));
                dto.setTelefono(T_ZATRP.getString(PDFZarpeConstantes.TFEMP));
                dto.setDiaHoraArriboPuerto(fechaArribo+"   "+ horaArribo);
                dto.setDiaHoraZarpe(fechaZarpe+"   "+ horaZarpe);


            }
            logger.error("RolTripulacion");
            String[] CamposRolTripulacion= new String[]{PDFZarpeConstantes.NOMBR,
                    PDFZarpeConstantes.NRLIB,
                    PDFZarpeConstantes.FEFVG,
                    PDFZarpeConstantes.STEXT};

            String[][] RolTripulacion=new String[T_DZATR.getNumRows()+1][CamposRolTripulacion.length];

            RolTripulacion[0]= PDFZarpeConstantes.fieldRolTripulacion;
            int con=1;
            for(int i=0; i<T_DZATR.getNumRows(); i++){
                T_DZATR.setRow(i);

                String[] registros=new String[CamposRolTripulacion.length+1];
                int campos=0;
                for(int j=0; j<registros.length; j++){
                    if(j==0){
                        registros[j]=String.valueOf(con);

                    }else {

                        if(campos==2){

                            try {
                                String fecha = ConvertirFecha(T_DZATR, PDFZarpeConstantes.FEFVG);
                                registros[j] = fecha;
                            }catch (Exception e){
                                registros[j] = T_DZATR.getString(CamposRolTripulacion[campos]);
                            }
                        }else if(campos==3){
                            registros[j] = T_DZATR.getString(CamposRolTripulacion[campos]).replace("/","");
                        }else {
                            registros[j] = T_DZATR.getString(CamposRolTripulacion[campos]);
                        }
                        if (registros[j].trim().compareTo("PATRON EP") == 0) {
                            dto.setNombreCapitanPatron(registros[1]);

                        }

                        campos++;
                    }
                }

                RolTripulacion[con]=registros;
                con++;
            }
            logger.error("Certificados");

            String[] CamposCertificados= new String[]{PDFZarpeConstantes.DSCER,
                                                        PDFZarpeConstantes.FECCF};

            String[][] Certificados=new String[T_VGCER.getNumRows()+1][CamposCertificados.length];
            Certificados[0]= PDFZarpeConstantes.fieldCertificados;
            logger.error("Certificados_1");
            con=1;
            for(int i=0; i<T_VGCER.getNumRows(); i++){
                T_VGCER.setRow(i);

                String[] registros=new String[CamposCertificados.length+1];
                int campos=0;
                for(int j=0; j<registros.length; j++){

                    if(j==0){
                        registros[j]=String.valueOf(con);
                    }else if(j==1) {

                        registros[j] = T_VGCER.getString(CamposCertificados[campos]);
                        campos++;


                    }else if(j==2){
                        if(registros[1].trim().compareTo("ARQUEO")==0){

                            registros[j]=T_VGCER.getString(PDFZarpeConstantes.NRCER);

                        }else if(registros[1].trim().compareTo("REGISTRO DE RADIOBALIZA")==0
                                || registros[1].trim().compareTo("MATRICULA DE NAVES")==0
                                || registros[1].trim().compareTo("COMPENSACION DE COMPAS")==0) {

                            registros[j] = T_VGCER.getString(PDFZarpeConstantes.DSETP);
                        }else{
                            registros[j] = T_VGCER.getString(CamposCertificados[campos]);
                        }
                        campos++;
                    }
                }

                Certificados[con]=registros;
                con++;
            }


            PlantillaPDFZarpeTravesia(path, dto, RolTripulacion, Certificados);

            Metodos exec = new Metodos();
            pdf.setBase64(exec.ConvertirABase64(path));
            pdf.setMensaje("Ok");


        }catch (Exception e){
            pdf.setMensaje(e.getMessage());
        }
        return pdf;
    }
    public void PlantillaPDFZarpeTravesia(String path, PDFZarpeTravesiaDto dto, String[][] rolTripulacion, String[][] certificados)throws Exception{

        logger.error("PlantillaPDF");

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);

        document.addPage(page);

        PDFont bold = PDType1Font.HELVETICA_BOLD;
        PDFont font = PDType1Font.HELVETICA;

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.beginText();
        contentStream.setFont(bold, 12);
        contentStream.moveTextPositionByAmount(180, 770);
        contentStream.drawString(PDFZarpeTravesiaConstantes.titulo);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(60, 755);
        contentStream.drawString(PDFZarpeTravesiaConstantes.capitania );
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(250, 755);
        contentStream.drawString(dto.getCapitaniaGuardacostas());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(250, 754);
        contentStream.drawString("___________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 743);
        contentStream.drawString(PDFZarpeTravesiaConstantes.uno);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(165, 743);
        contentStream.drawString(dto.getNombreNave());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(165, 742);
        contentStream.drawString("_______________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 731);
        contentStream.drawString(PDFZarpeTravesiaConstantes.dos);
        contentStream.endText();

        //insertando numero de matrícula
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(155, 731);
        contentStream.drawString(dto.getMatricula());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(155, 730);
        contentStream.drawString("_______________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(300, 731);
        contentStream.drawString(PDFZarpeTravesiaConstantes.tres);
        contentStream.endText();

        //insertando A.B.
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(334, 731);
        contentStream.drawString(dto.getArqueoBruto());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(334, 730);
        contentStream.drawString("_________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 719);
        contentStream.drawString(PDFZarpeTravesiaConstantes.cuatro);
        contentStream.endText();

        //insertando zona de pesca
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(155, 719);
        contentStream.drawString(dto.getColorCasco());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(155, 718);
        contentStream.drawString("_______________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(300, 719);
        contentStream.drawString(PDFZarpeTravesiaConstantes.cuatroB);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(400, 719);
        contentStream.drawString(dto.getSuperEstructura());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(400, 718);
        contentStream.drawString("_________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 707);
        contentStream.drawString(PDFZarpeTravesiaConstantes.cinco);
        contentStream.endText();

        //insertar tiempo de operacion
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(155, 707);
        contentStream.drawString(dto.getPropietario());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(155, 706);
        contentStream.drawString("_________________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 695);
        contentStream.drawString(PDFZarpeTravesiaConstantes.seis);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(155, 695);
        contentStream.drawString(dto.getDomicilioFiscal());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(155, 694);
        contentStream.drawString("_________________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 683);
        contentStream.drawString(PDFZarpeTravesiaConstantes.siete);
        contentStream.endText();

        //insertando representante acreditado
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(200, 683);
        contentStream.drawString(dto.getRepresentanteAcreditado());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(200, 682);
        contentStream.drawString("_______________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 671);
        contentStream.drawString(PDFZarpeTravesiaConstantes.ocho);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(155, 671);
        contentStream.drawString(dto.getDomicilioFiscal());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(155, 670);
        contentStream.drawString("_________________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 659);
        contentStream.drawString(PDFZarpeTravesiaConstantes.nueve);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(120, 659);
        contentStream.drawString(dto.getTelefono());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(120, 658);
        contentStream.drawString("__________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(220, 659);
        contentStream.drawString(PDFZarpeTravesiaConstantes.diez);
        contentStream.endText();

        /*
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(270, 659);
        contentStream.drawString(dto.getTelex());
        contentStream.endText();*/

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(270, 658);
        contentStream.drawString("________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(390, 659);
        contentStream.drawString(PDFZarpeTravesiaConstantes.once);
        contentStream.endText();


        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(430, 658);
        contentStream.drawString("___________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 647);
        contentStream.drawString(PDFZarpeTravesiaConstantes.doce);
        contentStream.endText();


        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 350);
        contentStream.drawString(PDFZarpeTravesiaConstantes.trece);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 12);
        contentStream.moveTextPositionByAmount(220, 200);
        contentStream.drawString(PDFZarpeTravesiaConstantes.planNavegacion);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 180);
        contentStream.drawString(PDFZarpeTravesiaConstantes.catorce);
        contentStream.endText();

        //insertando dni
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(160, 180);
        contentStream.drawString(dto.getDiaHoraZarpe());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(160, 179);
        contentStream.drawString("____________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(340, 180);
        contentStream.drawString(PDFZarpeTravesiaConstantes.quince);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(440, 179);
        contentStream.drawString("________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 165);
        contentStream.drawString(PDFZarpeTravesiaConstantes.dieciseis);
        contentStream.endText();

        /*
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(160, 165);
        contentStream.drawString(dto.getRumboInicial());
        contentStream.endText();*/

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(160, 164);
        contentStream.drawString("____________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 150);
        contentStream.drawString(PDFZarpeTravesiaConstantes.diecisiete);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(210, 150);
        contentStream.drawString(dto.getDiaHoraArriboPuerto());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(210, 149);
        contentStream.drawString("_________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 135);
        contentStream.drawString(PDFZarpeTravesiaConstantes.dieciocho);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(210, 134);
        contentStream.drawString("_________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(60, 120);
        contentStream.drawString(dto.getNombreCapitanPatron());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(60, 120);
        contentStream.drawString("_______________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(340, 135);
        contentStream.drawString(PDFZarpeTravesiaConstantes.diecinueve);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(440, 134);
        contentStream.drawString("________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(340, 120);
        contentStream.drawString("______________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 110);
        contentStream.drawString(PDFZarpeTravesiaConstantes.veinte);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(50, 80);
        contentStream.drawString("____________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(340, 110);
        contentStream.drawString(PDFZarpeTravesiaConstantes.veintiuno);
        contentStream.endText();


        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(340, 80);
        contentStream.drawString("______________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(50, 70);
        contentStream.drawString(PDFZarpeTravesiaConstantes.veintidos);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(100, 69);
        contentStream.drawString("_________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(340, 70);
        contentStream.drawString(PDFZarpeTravesiaConstantes.veintitres);
        contentStream.endText();


        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(420, 69);
        contentStream.drawString("____________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(45, 60);
        contentStream.drawString(PDFZarpeTravesiaConstantes.nota);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(45, 50);
        contentStream.drawString(PDFZarpeTravesiaConstantes.notaUno);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(45, 45);
        contentStream.drawString(PDFZarpeTravesiaConstantes.notaDos);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(45, 40);
        contentStream.drawString(PDFZarpeTravesiaConstantes.notaDos1);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(45, 35);
        contentStream.drawString(PDFZarpeTravesiaConstantes.notaTres);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(45, 30);
        contentStream.drawString(PDFZarpeTravesiaConstantes.notaTres1);
        contentStream.endText();

        logger.error("PlantillaPDF_1");
        drawTableRolTripulacion(page, contentStream, 642.0f, 50.0f, rolTripulacion);
        logger.error("PlantillaPDF_2");
        drawTableCertificados(contentStream,340, 50, certificados);
        logger.error("PlantillaPDF_3");

        contentStream.close();
        document.save(path);
        document.close();



    }


    public PDFExports GenerarPDFRolTripulacion(RolTripulacionImports imports)throws Exception{

        PDFExports pdf = new PDFExports();
        String path = Constantes.RUTA_ARCHIVO_IMPORTAR + "Archivo.pdf";
        PDFRolTripulacionDto dto= new PDFRolTripulacionDto();

         try {

            JCoDestination destination = JCoDestinationManager.getDestination(Constantes.DESTINATION_NAME);
            JCoRepository repo = destination.getRepository();
            JCoFunction stfcConnection = repo.getFunction(Constantes.ZFL_RFC_REGROL_ADM_REGROL);

            JCoParameterList importx = stfcConnection.getImportParameterList();
            importx.setValue("P_TOPE", imports.getP_tope());
            importx.setValue("P_CDRTR", imports.getP_cdrtr());
            importx.setValue("P_CANTI", imports.getP_canti());

            JCoParameterList tables = stfcConnection.getTableParameterList();
            stfcConnection.execute(destination);

            JCoTable T_ZARTR = tables.getTable(Tablas.T_ZARTR);
            JCoTable T_DZART = tables.getTable(Tablas.T_DZART);


            for(int i=0; i<T_ZARTR.getNumRows(); i++){
                T_ZARTR.setRow(i);

                String fecha=ConvertirFecha(T_ZARTR, PDFRolTripulacionConstantes.FERTR);

                dto.setNombreNave(T_ZARTR.getString(PDFRolTripulacionConstantes.DSWKS));
                dto.setMatricula(T_ZARTR.getString(PDFRolTripulacionConstantes.MREMB));
                dto.setArqueoBruto(T_ZARTR.getString(PDFRolTripulacionConstantes.AQBRT));
                dto.setArmador(T_ZARTR.getString(PDFRolTripulacionConstantes.DSEMP));
                dto.setArqueoNeto(T_ZARTR.getString(PDFRolTripulacionConstantes.AQNET));
                dto.setNumTripulantes(T_ZARTR.getString(PDFRolTripulacionConstantes.NRTRP));
                dto.setFecha(fecha);



            }
            logger.error("RolTripulacion");
            String[] CamposRolTripulacion= new String[]{PDFRolTripulacionConstantes.NOMBR,
                    PDFRolTripulacionConstantes.TITRP,
                    PDFRolTripulacionConstantes.STEXT,
                    PDFRolTripulacionConstantes.NRLIB,
                    PDFRolTripulacionConstantes.FEFVG};
            String[][] RolTripulacion=new String[T_DZART.getNumRows()+1][CamposRolTripulacion.length];

            RolTripulacion[0]= PDFRolTripulacionConstantes.fieldRolTripulacion;
            int con=1;
            for(int i=0; i<T_DZART.getNumRows(); i++){
                T_DZART.setRow(i);

                String[] registros=new String[CamposRolTripulacion.length+1];
                int campos=0;
                for(int j=0; j<registros.length; j++){
                    if(j==0){
                        registros[j]=String.valueOf(con);

                    }else {

                        if(campos==4){

                            try {
                                String fecha = ConvertirFecha(T_DZART, PDFRolTripulacionConstantes.FEFVG);
                                registros[j] = fecha;
                            }catch (Exception e){
                                registros[j] = T_DZART.getString(CamposRolTripulacion[campos]);
                            }
                        }else if(campos==2){
                            registros[j] = T_DZART.getString(CamposRolTripulacion[campos]).replace("/","");
                        }else {
                            registros[j] = T_DZART.getString(CamposRolTripulacion[campos]);
                        }

                        if (registros[j].trim().compareTo("PATRON EP") == 0) {
                            dto.setNombrePatron(registros[1]);

                        }

                        campos++;
                    }
                }

                RolTripulacion[con]=registros;
                con++;
            }



             PlantillaPDFRolTripulacion(path, dto, RolTripulacion);

            Metodos exec = new Metodos();
            pdf.setBase64(exec.ConvertirABase64(path));
            pdf.setMensaje("Ok");


        }catch (Exception e){
            pdf.setMensaje(e.getMessage());
        }



        return pdf;
    }

    public  void drawTableRolTripu(PDPage page, PDPageContentStream contentStream,
                                         float y, float margin, String[][] content) throws IOException {

        logger.error("drawTableRolTripulacion");
        final int rows = content.length;
        final int cols = content[0].length;
        final float rowHeight = 15.0f;
        final float tableWidth = page.getMediaBox().getWidth() - 2.0f * margin;
        final float tableHeight = rowHeight * (float) rows;
        final float colWidth = 94.33f;

        //draw the rows
        float nexty = y ;
        for (int i = 0; i <= rows; i++) {
            contentStream.moveTo(margin, nexty);
            contentStream.lineTo(margin + tableWidth, nexty);
            contentStream.stroke();
            nexty -= rowHeight;

        }


        //draw the columns
        float nextx = margin;
        for (int i = 0; i <= cols+2; i++) {

            if(i==1){
                nextx=margin+20;
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
            }else if(i==2){
                nextx=margin+200f;
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
            }
            else if(i==5 || i==6){
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
                nextx += colWidth-21;
            }else {
                contentStream.moveTo(nextx, y);
                contentStream.lineTo(nextx, y - tableHeight);
                contentStream.stroke();
                nextx += colWidth;
            }

        }


        //now add the text



        float texty=y-12;
        for(int i=0; i<content.length; i++) {

            String[]fields=content[i];
            float textx=margin+5;

            for (int j = 0; j < fields.length; j++) {

                switch (j) {
                    case 1:
                        if(i==0){
                            textx = 100;
                        }else {
                            textx = 58;
                        }
                        break;
                    case 2:
                        if(i==0){
                            textx=265;
                        }else {
                            textx = 250;
                        }
                        break;
                    case 3:
                        if(i==0){
                            textx = 340;
                        }else{
                            textx = 330;
                        }
                        break;
                    case 4:
                        if(i==0){
                            textx = 440;
                        }else {
                            textx = 425;
                        }
                        break;
                    case 5:
                        textx = 510;
                        break;
                }

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 6.5f);
                contentStream.newLineAtOffset(textx, texty);
                contentStream.showText(fields[j]);
                contentStream.endText();


            }
            texty-=15;
        }

    }
    public void PlantillaPDFRolTripulacion(String path, PDFRolTripulacionDto dto, String[][] rolTripulacion)throws Exception{

        logger.error("PlantillaPDF");

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);


        document.addPage(page);

        PDFont bold = PDType1Font.HELVETICA_BOLD;
        PDFont font = PDType1Font.HELVETICA;

        PDPageContentStream contentStream = new PDPageContentStream(document, page);


        contentStream.beginText();
        contentStream.setFont(bold, 12);
        contentStream.moveTextPositionByAmount(40, 760);
        contentStream.drawString(PDFRolTripulacionConstantes.titulo);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(35, 740);
        contentStream.drawString(PDFRolTripulacionConstantes.uno);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(150, 740);
        contentStream.drawString(dto.getNombreNave());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(150, 739);
        contentStream.drawString("___________________________________________________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(35, 725);
        contentStream.drawString(PDFRolTripulacionConstantes.dos);
        contentStream.endText();

        //insertando numero de matrícula
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(150, 725);
        contentStream.drawString(dto.getMatricula());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(150, 724);
        contentStream.drawString("_____________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(310, 725);
        contentStream.drawString(PDFRolTripulacionConstantes.tres);
        contentStream.endText();

        //insertando A.B.
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(400, 725);
        contentStream.drawString(dto.getArmador());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(400, 724);
        contentStream.drawString("___________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(35, 710);
        contentStream.drawString(PDFRolTripulacionConstantes.cuatro);
        contentStream.endText();

        //insertando zona de pesca
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(150, 710);
        contentStream.drawString(dto.getArqueoBruto());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(150, 709);
        contentStream.drawString("_____________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(310, 710);
        contentStream.drawString(PDFRolTripulacionConstantes.cinco);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(400, 710);
        contentStream.drawString(dto.getArqueoNeto());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(400, 709);
        contentStream.drawString("___________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(35, 695);
        contentStream.drawString(PDFRolTripulacionConstantes.seis);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(340, 695);
        contentStream.drawString(dto.getNumTripulantes());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(340, 694);
        contentStream.drawString("________________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(35, 250);
        contentStream.drawString(PDFRolTripulacionConstantes.siete);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(90, 250);
        contentStream.drawString(dto.getFecha());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(90, 249);
        contentStream.drawString("________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(310, 250);
        contentStream.drawString(PDFRolTripulacionConstantes.ocho);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(370, 249);
        contentStream.drawString("__________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(70, 200);
        contentStream.drawString("___________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(90, 190);
        contentStream.drawString(PDFRolTripulacionConstantes.patronEmbarcacion);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(350, 200);
        contentStream.drawString("__________________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(380, 190);
        contentStream.drawString(PDFRolTripulacionConstantes.verificadoPorGrado);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(70, 175);
        contentStream.drawString(PDFRolTripulacionConstantes.nombre);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(125, 175);
        contentStream.drawString("______________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(330, 175);
        contentStream.drawString(PDFRolTripulacionConstantes.nombre);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(380, 175);
        contentStream.drawString("______________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(70, 160);
        contentStream.drawString(PDFRolTripulacionConstantes.dni);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(125, 160);
        contentStream.drawString("______________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(330, 160);
        contentStream.drawString(PDFRolTripulacionConstantes.cip);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(380, 160);
        contentStream.drawString("______________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.moveTextPositionByAmount(210, 100);
        contentStream.drawString("_______________________________________");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.moveTextPositionByAmount(250, 90);
        contentStream.drawString(PDFRolTripulacionConstantes.capitanPuerto);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bold, 8);
        contentStream.moveTextPositionByAmount(35, 80);
        contentStream.drawString(PDFRolTripulacionConstantes.nota);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(35, 70);
        contentStream.drawString(PDFRolTripulacionConstantes.notaUno);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(35, 62);
        contentStream.drawString(PDFRolTripulacionConstantes.notaDos);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(35, 54);
        contentStream.drawString(PDFRolTripulacionConstantes.notaTres);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(35, 46);
        contentStream.drawString(PDFRolTripulacionConstantes.notaTres1);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 6);
        contentStream.moveTextPositionByAmount(35, 38);
        contentStream.drawString(PDFRolTripulacionConstantes.notaCuatro);
        contentStream.endText();

        logger.error("PlantillaPDF_1");
        drawTableRolTripu(page, contentStream, 685.0f, 30.0f, rolTripulacion);

        contentStream.close();
        document.save(path);
        document.close();

    }

    public String ConvertirFecha(JCoTable jCoTable, String tabla){

        String fecha="";

        try {
            JCoField fieldF = jCoTable.getField(tabla);
            Date date=fieldF.getDate();
            SimpleDateFormat dia = new SimpleDateFormat("dd/MM/yyyy");
            fecha = dia.format(date);
        }catch (Exception e){
            fecha="";
        }


        return fecha;
    }

    public String ConvertirHora(JCoTable jCoTable, String tabla){

        String hora="";
        try {
            JCoField fieldH = jCoTable.getField(tabla);
            Date time = fieldH.getTime();
            SimpleDateFormat hour = new SimpleDateFormat("HH:mm:ss");
            hora = hour.format(time);
        }catch (Exception e){
            hora="";
        }

        return hora;
    }


}
