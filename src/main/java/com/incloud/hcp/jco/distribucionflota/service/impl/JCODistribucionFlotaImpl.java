package com.incloud.hcp.jco.distribucionflota.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incloud.hcp.jco.distribucionflota.dto.*;
import com.incloud.hcp.jco.distribucionflota.service.JCODistribucionFlotaService;
import com.incloud.hcp.jco.maestro.dto.MaestroExport;
import com.incloud.hcp.jco.maestro.dto.MaestroImportsKey;
import com.incloud.hcp.jco.maestro.dto.MaestroOptions;
import com.incloud.hcp.jco.maestro.dto.MaestroOptionsKey;
import com.incloud.hcp.jco.maestro.service.JCOMaestrosService;
import com.incloud.hcp.jco.maestro.service.impl.JCOMaestrosServiceImpl;
import com.incloud.hcp.util.Constantes;
import com.incloud.hcp.util.EjecutarRFC;
import com.incloud.hcp.util.Tablas;
import com.sap.conn.jco.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class JCODistribucionFlotaImpl implements JCODistribucionFlotaService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private JCOMaestrosService MaestroService;

    @Override
    public DistribucionFlotaExports ListarDistribucionFlota(DistribucionFlotaImports importsParam) throws Exception {

        DistribucionFlotaExports dto = new DistribucionFlotaExports();

        try {

            /******************************** LLAMADA AL RFC DE DISTRIBUCION DE FLOTA ***********************/

            HashMap<String, Object> imports = new HashMap<String, Object>();
            imports.put("P_USER", importsParam.getP_user());
            imports.put("P_INPRP", importsParam.getP_inprp());
            imports.put("P_INUBC", importsParam.getP_inubc());
            imports.put("P_CDTEM", importsParam.getP_cdtem());
            logger.error("ListarDistribucionFlota1");

            EjecutarRFC exec = new EjecutarRFC();
            JCoDestination destination = JCoDestinationManager.getDestination(Constantes.DESTINATION_NAME);

            JCoRepository repo = destination.getRepository();
            JCoFunction function = repo.getFunction(Constantes.ZFL_RFC_DISTR_FLOTA);
            exec.setImports(function, imports);

            JCoParameterList jcoTables = function.getTableParameterList();
            function.execute(destination);

            JCoTable s_str_zlt = jcoTables.getTable(Tablas.STR_ZLT);
            JCoTable s_str_di = jcoTables.getTable(Tablas.STR_DI);
            JCoTable s_str_pta = jcoTables.getTable(Tablas.STR_PTA);
            JCoTable s_str_dp = jcoTables.getTable(Tablas.STR_DP);

            /* ----------------------------------- Fin de consumo de RFC ----------------------------------*/

            /************************************* lOGICA DE ARMADO DE REQUEST *****************************/

            List<ZonasDto> zonas = new ArrayList<ZonasDto>();
            List<DescargasDto> Descargas = new ArrayList<DescargasDto>();
            double vd_totdesc_cbod = 0;
            double vd_totdesc_decl = 0;
            double vd_totdesc_desc = 0;
            for (int i = 0; i < s_str_zlt.getNumRows(); i++) {
                s_str_zlt.setRow(i);
                ZonasDto n_zona = new ZonasDto();
                n_zona.setZonaName(s_str_zlt.getString("DSZLT"));
                List<PlantasDto> plantas = new ArrayList<PlantasDto>();
                for (int j = 0; j < s_str_pta.getNumRows(); j++) {
                    s_str_pta.setRow(j);
                    PlantasDto n_planta = new PlantasDto();
                    String cz1 = s_str_pta.getString("CDZLT");
                    String cz2 = s_str_zlt.getString("CDZLT");
                    logger.error("cz1 : " + cz1 + " - cz2 : " + cz2);
                    if(s_str_pta.getString("CDZLT").equalsIgnoreCase(s_str_zlt.getString("CDZLT"))){
                        logger.error("cz12 : " + cz1 + " - cz22 : " + cz2);
                        n_planta.setPlantaName(s_str_pta.getString("DESCR"));
                        n_planta.setTot_PescaReq(s_str_pta.getString("CNPRQ"));
                        List<EmbarcacionesDto> embarcaciones = new ArrayList<EmbarcacionesDto>();
                        //VARIABLES TOTALES
                        int vi_contadorEmb = 0;
                        double vi_contadorBod = 0;
                        double vi_contadorDecl = 0;
                        int vi_totdesc_est = 0;
                        for (int k = 0; k < s_str_di.getNumRows(); k++) {
                            s_str_di.setRow(k);
                            EmbarcacionesDto n_embarcacion =  new EmbarcacionesDto();
                            if(s_str_di.getString("CDPTA").equalsIgnoreCase(s_str_pta.getString("CDPTA"))){
                                //CARGA DEL TAP DESC
                                boolean bOk = true;
                                String estMarea = s_str_di.getString("ESMAR");
                                String estMareaCie = s_str_di.getString("ESCMA");
                                String embaNomin = s_str_di.getString("EMPTO");
                                String indProp = s_str_di.getString("INPRP");

                                if (embaNomin.equalsIgnoreCase("N") && indProp.equalsIgnoreCase("P")) {
                                    bOk = false;

                                }

                                if((estMarea.equalsIgnoreCase("C") && estMareaCie.equalsIgnoreCase("T")) && bOk) {
                                    DescargasDto o_descarga = new DescargasDto();

                                    o_descarga.setCbodEmba(s_str_di.getString("CPPMS"));
                                    o_descarga.setCodEmba(s_str_di.getString("CDEMB"));
                                    o_descarga.setCodPlanta(s_str_di.getString("CDPTA"));
                                    o_descarga.setDescEmba(s_str_di.getString("NMEMB"));
                                    o_descarga.setDescPlanta(s_str_di.getString("DESCR"));
                                    o_descarga.setPescDecl(s_str_di.getString("CNPCM"));
                                    o_descarga.setPescDesc(s_str_di.getString("CNPDS"));

                                    Descargas.add(o_descarga);

                                    vd_totdesc_decl = vd_totdesc_decl + Double.parseDouble(o_descarga.getPescDecl());
                                    vd_totdesc_desc = vd_totdesc_desc + Double.parseDouble(o_descarga.getPescDesc());
                                    vd_totdesc_cbod = vd_totdesc_cbod + Double.parseDouble(o_descarga.getCbodEmba());

                                }

                                //SUMATORIA DE VARIABLES TOTALES
                                vi_contadorEmb++;
                                vi_contadorBod = Double.parseDouble(s_str_di.getString("CPPMS")) + vi_contadorBod;
                                vi_contadorDecl = Double.parseDouble(s_str_di.getString("CNPCM")) + vi_contadorDecl;
                                if(s_str_di.getString("DSEEC").equalsIgnoreCase("ESDE")){
                                    vi_totdesc_est++;
                                }

                                n_embarcacion.setDescEmba(s_str_di.getString("NMEMB"));
                                n_embarcacion.setCbodEmba(s_str_di.getString("CPPMS"));
                                n_embarcacion.setPescDecl(s_str_di.getString("CNPCM"));
                                n_embarcacion.setEstado(s_str_di.getString("DSEEC"));
                                n_embarcacion.setHoraArribo(s_str_di.getString("HEARR"));
                                n_embarcacion.setDiaAnt(s_str_di.getString("DA"));
                                n_embarcacion.setTdc(s_str_di.getString("TDC"));
                                n_embarcacion.setDescZonaCala(s_str_di.getString("ZONA"));
                                n_embarcacion.setEstSisFrio(s_str_di.getString("ESTSF"));
                                n_embarcacion.setColor(s_str_di.getString("CLGFL"));

                                String CodMotMarea = s_str_di.getString("CDMMA");
                                String TDC = s_str_di.getString("TDC");
                                BigDecimal n_TDC;
                                if(TDC != null && !TDC.isEmpty()){
                                    n_TDC = new BigDecimal(TDC);
                                }else{
                                    n_TDC = new BigDecimal("0");
                                }
                                if (CodMotMarea.equalsIgnoreCase("2")){ //CHI
                                    if (n_TDC.compareTo(new BigDecimal(12)) == -1){
                                        n_embarcacion.setSemaforo("images/green_line.gif");
                                    } else if ((n_TDC.compareTo(new BigDecimal(13)) == -1) || (n_TDC.compareTo(new BigDecimal(13)) == 0)){
                                        n_embarcacion.setSemaforo("images/yellow_line.gif");
                                    } else if ((n_TDC.compareTo(new BigDecimal(13)) == 1)){
                                        n_embarcacion.setSemaforo("images/red_line.gif");
                                    }
                                } else if (CodMotMarea.equalsIgnoreCase("1")){ //CHD
                                    if (n_TDC.compareTo(new BigDecimal(16)) == -1){
                                        n_embarcacion.setSemaforo("images/green_line.gif");
                                    } else if ((n_TDC.compareTo(new BigDecimal(17)) == -1) || (n_TDC.compareTo(new BigDecimal(17)) == 0)){
                                        n_embarcacion.setSemaforo("images/yellow_line.gif");
                                    } else if ((n_TDC.compareTo(new BigDecimal(17)) == 1)){
                                        n_embarcacion.setSemaforo("images/red_line.gif");
                                    }
                                }

                                embarcaciones.add(n_embarcacion);
                            }

                        }
                        n_planta.setTot_emb(String.valueOf(vi_contadorEmb));
                        n_planta.setTot_bod(String.valueOf(vi_contadorBod));
                        n_planta.setTot_decl(String.valueOf(vi_contadorDecl));
                        n_planta.setTot_Est(String.valueOf(vi_totdesc_est));
                        n_planta.setListaEmbarcaciones(embarcaciones);
                        plantas.add(n_planta);
                    }
                }
                logger.error("cantidad de plantas : " + plantas.size());
                n_zona.setListaPlantas(plantas);
                zonas.add(n_zona);
            }
            /* ----------------------------------- Fin de armado de JSON----------------------------------*/
            //String prueba = "";
            //prueba.compareTo();
            dto.setListaZonas(zonas);
            /************************************* lOGICA DE SETEO DE RESUMEN *****************************/
            dto.setListaDescargas(Descargas);
            dto.setTot_desc_cbod(String.valueOf(vd_totdesc_cbod));
            dto.setTot_desc_desc(String.valueOf(vd_totdesc_desc));
            dto.setTot_desc_dscl(String.valueOf(vd_totdesc_decl));

            List<PropiosDto> lst_propios = new ArrayList<PropiosDto>();
            List<TercerosDto> lst_terceros = new ArrayList<TercerosDto>();
            List<TotalDto> lst_totales = new ArrayList<TotalDto>();

            double vd_totterc_cbod= 0;
            double vd_totterc_dscl = 0;
            double vd_totterc_ep = 0;
            double vd_totprop_cbod= 0;
            double vd_totprop_dscl = 0;
            double vd_totprop_ep = 0;
            double vd_tottot_cbod= 0;
            double vd_tottot_dscl = 0;
            double vd_tottot_ep = 0;

            for (int i = 0; i < s_str_dp.getNumRows(); i++) {
                s_str_dp.setRow(i);
                TotalDto resumenTotal = new TotalDto();
                BigDecimal b_cnpcm = new BigDecimal(s_str_dp.getString("CNPCM"));
                BigDecimal b_cnpdt = new BigDecimal(s_str_dp.getString("CNPDT"));


               // MaestroExport maestroPl =  this.MaestroService.obtenerMaestro2();

                if (b_cnpcm.compareTo(new BigDecimal(0))> 0) {

                    MaestroImportsKey maestro = new MaestroImportsKey();
                    String[] fields = {"CXPXD"};
                    List<MaestroOptions> options = new ArrayList<MaestroOptions>();
                    List<MaestroOptionsKey> options2 = new ArrayList<MaestroOptionsKey>();

                    MaestroOptions item_option = new MaestroOptions();
                    String v_planta = s_str_dp.getString("DESCR");
                    item_option.setWa("DESCR ='"+ v_planta + "'");
                    options.add(item_option);

                    maestro.setDelimitador("|");
                    maestro.setFields(fields);
                    maestro.setNo_data("");
                    maestro.setOption(options);
                    maestro.setOptions(options2);
                    maestro.setOrder("");
                    maestro.setP_user(importsParam.getP_user());
                    maestro.setRowcount(0);
                    maestro.setRowskips(0);
                    maestro.setTabla("ZFLPTA");
                    String v_PescDecl = s_str_dp.getString("CNPCM");
                    String v_dif = this.prueba(maestro,importsParam.getP_user(),v_PescDecl);
                    resumenTotal.setDif(v_dif);

                    /*-----------------------------------------------------------*/
                    PropiosDto resumenProp = new PropiosDto();

                    resumenProp.setDescPlanta(s_str_dp.getString("DESCR"));
                    resumenProp.setPescDeclProp(s_str_dp.getString("CNPCM"));
                    resumenProp.setEmbaPescProp(s_str_dp.getString("CNEMB"));
                    resumenProp.setCbodProp(s_str_dp.getString("CPPMP"));

                    resumenTotal.setDescPlanta(s_str_dp.getString("DESCR"));
                    resumenTotal.setPescDeclProp(s_str_dp.getString("CNPCM"));
                    resumenTotal.setEmbaPescProp(s_str_dp.getString("CNEMB"));
                    resumenTotal.setCbodProp(s_str_dp.getString("CPPMP"));

                    vd_totprop_cbod = vd_totprop_cbod + Double.parseDouble(resumenProp.getCbodProp());
                    vd_totprop_dscl = vd_totprop_dscl + Double.parseDouble(resumenProp.getPescDeclProp());
                    vd_totprop_ep = vd_totprop_ep + Double.parseDouble(resumenProp.getEmbaPescProp());

                    vd_tottot_cbod = vd_tottot_cbod + Double.parseDouble(resumenProp.getCbodProp());
                    vd_tottot_dscl = vd_tottot_dscl + Double.parseDouble(resumenProp.getPescDeclProp());
                    vd_tottot_ep = vd_tottot_ep + Double.parseDouble(resumenProp.getEmbaPescProp());

                    lst_propios.add(resumenProp);
                    lst_totales.add(resumenTotal);

                }

                if (b_cnpdt.compareTo(new BigDecimal(0))> 0) {

                    MaestroImportsKey maestro = new MaestroImportsKey();
                    String[] fields = {"CXPXD"};
                    List<MaestroOptions> options = new ArrayList<MaestroOptions>();
                    List<MaestroOptionsKey> options2 = new ArrayList<MaestroOptionsKey>();

                    MaestroOptions item_option = new MaestroOptions();
                    String v_planta = s_str_dp.getString("DESCR");
                    item_option.setWa("DESCR ='"+ v_planta + "'");
                    options.add(item_option);

                    maestro.setDelimitador("|");
                    maestro.setFields(fields);
                    maestro.setNo_data("");
                    maestro.setOption(options);
                    maestro.setOptions(options2);
                    maestro.setOrder("");
                    maestro.setP_user(importsParam.getP_user());
                    maestro.setRowcount(0);
                    maestro.setRowskips(0);
                    maestro.setTabla("ZFLPTA");
                    String v_PescDecl = s_str_dp.getString("CNPDT");
                    String v_dif = this.prueba(maestro,importsParam.getP_user(),v_PescDecl);
                    resumenTotal.setDif(v_dif);

                    /*-----------------------------------------------------------*/

                    TercerosDto resumenTerc = new TercerosDto();

                    resumenTerc.setDescPlanta(s_str_dp.getString("DESCR"));
                    resumenTerc.setPescDeclProp(s_str_dp.getString("CNPDT"));
                    resumenTerc.setEmbaPescProp(s_str_dp.getString("CNEMT"));
                    resumenTerc.setCbodProp(s_str_dp.getString("CPPMT"));

                    resumenTotal.setDescPlanta(s_str_dp.getString("DESCR"));
                    resumenTotal.setPescDeclProp(s_str_dp.getString("CNPDT"));
                    resumenTotal.setEmbaPescProp(s_str_dp.getString("CNEMT"));
                    resumenTotal.setCbodProp(s_str_dp.getString("CPPMT"));

                    vd_totterc_cbod = vd_totterc_cbod + Double.parseDouble(resumenTerc.getCbodProp());
                    vd_totterc_dscl = vd_totterc_dscl + Double.parseDouble(resumenTerc.getPescDeclProp());
                    vd_totterc_ep = vd_totterc_ep + Double.parseDouble(resumenTerc.getEmbaPescProp());

                    vd_tottot_cbod = vd_tottot_cbod + Double.parseDouble(resumenTerc.getCbodProp());
                    vd_tottot_dscl = vd_tottot_dscl + Double.parseDouble(resumenTerc.getPescDeclProp());
                    vd_tottot_ep = vd_tottot_ep + Double.parseDouble(resumenTerc.getEmbaPescProp());

                    lst_terceros.add(resumenTerc);
                    lst_totales.add(resumenTotal);

                }

            }

            dto.setListaPropios(lst_propios);
            dto.setTot_prop_cbod(String.valueOf(vd_totprop_cbod));
            dto.setTot_prop_dscl(String.valueOf(vd_totprop_dscl));
            dto.setTot_prop_ep(String.valueOf(vd_totprop_ep));
            dto.setListaTerceros(lst_terceros);
            dto.setTot_terc_cbod(String.valueOf(vd_totterc_cbod));
            dto.setTot_terc_dscl(String.valueOf(vd_totterc_dscl));
            dto.setTot_terc_ep(String.valueOf(vd_totterc_ep));
            dto.setListaTotal(lst_totales);
            dto.setTot_tot_cbod(String.valueOf(vd_tottot_cbod));
            dto.setTot_tot_dscl(String.valueOf(vd_tottot_dscl));
            dto.setTot_tot_ep(String.valueOf(vd_tottot_ep));
            /*----------------------------------- Fin de SETEO de resumen----------------------------------*/

        }catch (Exception e){
            dto.setMensaje(e.getMessage());
        }
        return dto;

    }


    public DistrFlotaOptExport ListarDistFltColumDinamic(DistrFlotaOptions importsParam) throws Exception {

        DistrFlotaOptExport dto = new DistrFlotaOptExport();

        try {

            /******************************** LLAMADA AL RFC DE DISTRIBUCION DE FLOTA ***********************/

            HashMap<String, Object> imports = new HashMap<String, Object>();
            imports.put("P_USER", importsParam.getP_user());
            imports.put("P_INPRP", importsParam.getP_inprp());
            imports.put("P_INUBC", importsParam.getP_inubc());
            imports.put("P_CDTEM", importsParam.getP_cdtem());
            logger.error("ListarDistribucionFlota1");

            EjecutarRFC exec = new EjecutarRFC();
            JCoDestination destination = JCoDestinationManager.getDestination(Constantes.DESTINATION_NAME);

            JCoRepository repo = destination.getRepository();
            JCoFunction function = repo.getFunction(Constantes.ZFL_RFC_DISTR_FLOTA);
            exec.setImports(function, imports);

            JCoParameterList jcoTables = function.getTableParameterList();
            function.execute(destination);

            JCoTable s_str_zlt = jcoTables.getTable(Tablas.STR_ZLT);
            JCoTable s_str_di = jcoTables.getTable(Tablas.STR_DI);
            JCoTable s_str_pta = jcoTables.getTable(Tablas.STR_PTA);
            JCoTable s_str_dp = jcoTables.getTable(Tablas.STR_DP);

            /* ----------------------------------- Fin de consumo de RFC ----------------------------------*/

            /*Metodos metodo = new Metodos();
            List<HashMap<String, Object>> Listar_s_str_zlt= metodo.ObtenerListObjetos(s_str_zlt,marea.getFieldMarea());
            List<HashMap<String, Object>> Listar_s_str_di= metodo.ObtenerListObjetos(S_EVENTO,marea.getFieldEvento());
            List<HashMap<String, Object>> Listar_s_str_pta= metodo.ObtenerListObjetos(STR_FLBSP,marea.getFieldFLBSP());
            List<HashMap<String, Object>> Listar_s_str_dp= metodo.ObtenerListObjetos(STR_PSCINC,marea.getFieldPSCINC());*/

            /************************************* lOGICA DE ARMADO DE REQUEST *****************************/

            List<ZonasDto> zonas = new ArrayList<ZonasDto>();
            for (int i = 0; i < s_str_zlt.getNumRows(); i++) {
                s_str_zlt.setRow(i);
                ZonasDto n_zona = new ZonasDto();
                n_zona.setZonaName(s_str_zlt.getString("DSZLT"));
                List<PlantasDto> plantas = new ArrayList<PlantasDto>();
                for (int j = 0; j < s_str_pta.getNumRows(); j++) {
                    s_str_pta.setRow(j);
                    PlantasDto n_planta = new PlantasDto();
                    String cz1 = s_str_pta.getString("CDZLT");
                    String cz2 = s_str_zlt.getString("CDZLT");
                    logger.error("cz1 : " + cz1 + " - cz2 : " + cz2);
                    if(s_str_pta.getString("CDZLT").equalsIgnoreCase(s_str_zlt.getString("CDZLT"))){
                        logger.error("cz12 : " + cz1 + " - cz22 : " + cz2);
                        n_planta.setPlantaName(s_str_pta.getString("DESCR"));
                        List<EmbarcacionesDto> embarcaciones = new ArrayList<EmbarcacionesDto>();
                        for (int k = 0; k < s_str_di.getNumRows(); k++) {
                            s_str_di.setRow(k);
                            EmbarcacionesDto n_embarcacion =  new EmbarcacionesDto();
                            if(s_str_di.getString("CDPTA").equalsIgnoreCase(s_str_pta.getString("CDPTA"))){
                                //n_embarcacion.setFlagEmba(s_str_di.);
                                n_embarcacion.setDescEmba(s_str_di.getString("NMEMB"));
                                n_embarcacion.setCbodEmba(s_str_di.getString("CDEMB"));
                                n_embarcacion.setPescDecl(s_str_di.getString("CNPCM"));
                                n_embarcacion.setEstado(s_str_di.getString("DSEEC"));
                                n_embarcacion.setHoraArribo(s_str_di.getString("HEARR"));
                                n_embarcacion.setDiaAnt(s_str_di.getString("DA"));
                                n_embarcacion.setTdc(s_str_di.getString("TDC"));
                                n_embarcacion.setDescZonaCala(s_str_di.getString("ZONA"));
                                n_embarcacion.setEstSisFrio(s_str_di.getString("ESTSF"));

                                String CodMotMarea = s_str_di.getString("CDMMA");
                                String TDC = s_str_di.getString("TDC");
                                BigDecimal n_TDC;
                                if(TDC != null || !TDC.isEmpty()){
                                    n_TDC = new BigDecimal(TDC);
                                }else{
                                    n_TDC = new BigDecimal("0");
                                }
                                if (CodMotMarea.equalsIgnoreCase("2")){ //CHI
                                    if (n_TDC.compareTo(new BigDecimal(12)) == -1){
                                        n_embarcacion.setSemaforo("images/green_line.gif");
                                    } else if ((n_TDC.compareTo(new BigDecimal(13)) == -1) || (n_TDC.compareTo(new BigDecimal(13)) == 0)){
                                        n_embarcacion.setSemaforo("images/yellow_line.gif");
                                    } else if ((n_TDC.compareTo(new BigDecimal(13)) == 1)){
                                        n_embarcacion.setSemaforo("images/red_line.gif");
                                    }
                                } else if (CodMotMarea.equalsIgnoreCase("1")){ //CHD
                                    if (n_TDC.compareTo(new BigDecimal(16)) == -1){
                                        n_embarcacion.setSemaforo("images/green_line.gif");
                                    } else if ((n_TDC.compareTo(new BigDecimal(17)) == -1) || (n_TDC.compareTo(new BigDecimal(17)) == 0)){
                                        n_embarcacion.setSemaforo("images/yellow_line.gif");
                                    } else if ((n_TDC.compareTo(new BigDecimal(17)) == 1)){
                                        n_embarcacion.setSemaforo("images/red_line.gif");
                                    }
                                }

                                embarcaciones.add(n_embarcacion);
                            }

                        }
                        n_planta.setListaEmbarcaciones(embarcaciones);
                        plantas.add(n_planta);
                    }
                }
                logger.error("cantidad de plantas : " + plantas.size());
                n_zona.setListaPlantas(plantas);
                zonas.add(n_zona);
            }
            /* ----------------------------------- Fin de armado de JSON----------------------------------*/
            //String prueba = "";
            //prueba.compareTo();
            //dto.setListaZonas(zonas);

        }catch (Exception e){
            dto.setMensaje(e.getMessage());
        }
        return dto;

    }


    public String prueba(MaestroImportsKey p_maestro,String p_user, String p_pescDecl){
    try {
        String r_diferencia = "";
        String v_cxpxd = "";
        String v_porcMaxProc = "";
        String v_horasProc = "";
        ObjectMapper objectMapper = new ObjectMapper();
        String usuarioJson = objectMapper.writeValueAsString(p_maestro);
        logger.error("JSON : " + usuarioJson);

        JCOMaestrosServiceImpl impMaestro = new JCOMaestrosServiceImpl();
        MaestroExport maestroPl =  impMaestro.obtenerMaestro2(p_maestro);
        logger.error("Prueba01");
        List<HashMap<String, Object>> list = maestroPl.getData();
        logger.error("cantidad de REG MET : " + list.size());
        for (HashMap<String, Object> map : list) {
            for (HashMap.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(key.equalsIgnoreCase("CXPXD")){
                    v_cxpxd = String.valueOf(value);
                }
            }

        }
        /*---------------------------------------------------------------------------------------------------------------*/
        MaestroImportsKey maestro_cons1 = new MaestroImportsKey();
        String[] fields_cons1 = {"VAL01"};
        List<MaestroOptions> options_cons1 = new ArrayList<MaestroOptions>();
        List<MaestroOptionsKey> options2_cons1 = new ArrayList<MaestroOptionsKey>();

        MaestroOptions item_option_cons1 = new MaestroOptions();
        item_option_cons1.setWa("CDCNS ='64'");
        options_cons1.add(item_option_cons1);

        maestro_cons1.setDelimitador("|");
        maestro_cons1.setFields(fields_cons1);
        maestro_cons1.setNo_data("");
        maestro_cons1.setOption(options_cons1);
        maestro_cons1.setOptions(options2_cons1);
        maestro_cons1.setOrder("");
        maestro_cons1.setP_user(p_user);
        maestro_cons1.setRowcount(0);
        maestro_cons1.setRowskips(0);
        maestro_cons1.setTabla("ZFLCNS");

        MaestroExport maestro_const1 =  impMaestro.obtenerMaestro2(maestro_cons1);
        List<HashMap<String, Object>> list_cons1 = maestro_const1.getData();
        for (HashMap<String, Object> map : list_cons1) {
            for (HashMap.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(key.equalsIgnoreCase("VAL01")){
                    v_porcMaxProc = String.valueOf(value);
                }
            }

        }
        /*---------------------------------------------------------------------------------------------------------------*/
        MaestroImportsKey maestro_cons2 = new MaestroImportsKey();
        String[] fields_cons2 = {"VAL01"};
        List<MaestroOptions> options_cons2 = new ArrayList<MaestroOptions>();
        List<MaestroOptionsKey> options2_cons2 = new ArrayList<MaestroOptionsKey>();

        MaestroOptions item_option_cons2 = new MaestroOptions();
        item_option_cons2.setWa("CDCNS ='65'");
        options_cons2.add(item_option_cons2);

        maestro_cons2.setDelimitador("|");
        maestro_cons2.setFields(fields_cons2);
        maestro_cons2.setNo_data("");
        maestro_cons2.setOption(options_cons2);
        maestro_cons2.setOptions(options2_cons2);
        maestro_cons2.setOrder("");
        maestro_cons2.setP_user(p_user);
        maestro_cons2.setRowcount(0);
        maestro_cons2.setRowskips(0);
        maestro_cons2.setTabla("ZFLCNS");

        MaestroExport maestro_const2 =  impMaestro.obtenerMaestro2(maestro_cons2);
        List<HashMap<String, Object>> list_cons2 = maestro_const2.getData();
        for (HashMap<String, Object> map : list_cons2) {
            for (HashMap.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(key.equalsIgnoreCase("VAL01")){
                    v_horasProc = String.valueOf(value);
                }
            }

        }
        /*------------------------------------------------------------------------------------------------------*/
        BigDecimal capProc = new BigDecimal(v_cxpxd);
        BigDecimal porcMaxProcBD = new BigDecimal(v_porcMaxProc);
        BigDecimal horasProcBD = new BigDecimal(v_horasProc);
        BigDecimal calculado = capProc.multiply(porcMaxProcBD).multiply(horasProcBD) ;
        BigDecimal declarado = new BigDecimal(p_pescDecl);
        BigDecimal diferencia = declarado.subtract(calculado) ;

        if (diferencia.compareTo(new BigDecimal(0)) == -1){
            diferencia = new BigDecimal(0);
            r_diferencia = String.valueOf(diferencia);
        } else {
            r_diferencia = String.valueOf(diferencia);
        }

        return r_diferencia;

    }catch(Exception e){
        String mensaje = e.getMessage();
        return mensaje;
    }

    }

}
