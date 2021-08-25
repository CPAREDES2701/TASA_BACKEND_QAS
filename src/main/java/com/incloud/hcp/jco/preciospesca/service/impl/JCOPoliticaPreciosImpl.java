package com.incloud.hcp.jco.preciospesca.service.impl;

import com.incloud.hcp.jco.preciospesca.dto.*;
import com.incloud.hcp.jco.preciospesca.service.JCOPoliticaPreciosService;
import com.incloud.hcp.util.Constantes;
import com.incloud.hcp.util.EjecutarRFC;
import com.incloud.hcp.util.Metodos;
import com.incloud.hcp.util.Tablas;
import com.sap.conn.jco.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class JCOPoliticaPreciosImpl implements JCOPoliticaPreciosService {

    @Override
    public PrecioPescaExports ObtenerPrecioPesca(PrecioPescaImports imports) throws Exception {
        HashMap<String, Object> importParams = new HashMap<>();
        importParams.put("P_USER", imports.getP_user());

        //Obtener los options
        List<HashMap<String, Object>> options = new ArrayList<HashMap<String, Object>>();
        for (MaestroOptionsPrecioPesca option : imports.getP_options()) {
            HashMap<String, Object> optionRecord = new HashMap<>();
            optionRecord.put("WA", option.getWa());
            options.add(optionRecord);
        }

        JCoDestination destination = JCoDestinationManager.getDestination(Constantes.DESTINATION_NAME);
        JCoRepository repo = destination.getRepository();
        JCoFunction function = repo.getFunction(Constantes.ZFL_RFC_LECT_PREC_PESC);

        JCoParameterList paramsTable = function.getTableParameterList();

        EjecutarRFC executeRFC = new EjecutarRFC();
        executeRFC.setImports(function, importParams);
        executeRFC.setTable(paramsTable, "P_OPTIONS", options);

        JCoParameterList tables = function.getTableParameterList();
        function.execute(destination);
        JCoTable tblSTR_PPC = tables.getTable(Tablas.STR_PPC);

        Metodos metodos = new Metodos();
        List<HashMap<String, Object>> listSTR_PPC = metodos.ListarObjetos(tblSTR_PPC);

        PrecioPescaExports dto = new PrecioPescaExports();
        dto.setStr_ppc(listSTR_PPC);
        dto.setMensaje("OK");

        return dto;
    }

    @Override
    public PrecioPescaMantExports MantPrecioPesca(PrecioPescaMantImports imports) throws Exception {
        HashMap<String, Object> importParams = new HashMap<>();
        importParams.put("P_USER", imports.getP_user());

        // Obtener los parámetros del PPC
        List<HashMap<String, Object>> str_ppcs = new ArrayList<>();
        for (PoliticaPrecios politicaPrecios : imports.getStr_ppc()) {
            HashMap<String, Object> ppcRecord = new HashMap<>();
            ppcRecord.put("MANDT", politicaPrecios.getMandt());
            ppcRecord.put("CDPPC", politicaPrecios.getCdppc());
            ppcRecord.put("CDZLT", politicaPrecios.getCdzlt());
            ppcRecord.put("CDPTO", politicaPrecios.getCdpto());
            ppcRecord.put("CDPTA", politicaPrecios.getCdpta());
            ppcRecord.put("CDEMP", politicaPrecios.getCdemp());
            ppcRecord.put("CDSPC", politicaPrecios.getCdspc());
            ppcRecord.put("FFVIG", politicaPrecios.getFfvig());
            ppcRecord.put("FIVIG", politicaPrecios.getFivig());
            ppcRecord.put("PRCMX", politicaPrecios.getPrcmx());
            ppcRecord.put("PRVMN", politicaPrecios.getPrvmn());
            ppcRecord.put("PRCTP", politicaPrecios.getPrctp());
            ppcRecord.put("PRVTP", politicaPrecios.getPrvtp());
            ppcRecord.put("WAERS", politicaPrecios.getWaers());
            ppcRecord.put("ESPMR", politicaPrecios.getEspmr());
            ppcRecord.put("FHCRN", politicaPrecios.getFhcrn());
            ppcRecord.put("HRCRN", politicaPrecios.getHrcrn());
            ppcRecord.put("ATCRN", politicaPrecios.getAtcrn());

            str_ppcs.add(ppcRecord);
        }

        JCoDestination destination = JCoDestinationManager.getDestination(Constantes.DESTINATION_NAME);
        JCoRepository repo = destination.getRepository();
        JCoFunction function = repo.getFunction(Constantes.ZFL_RFC_MANT_PRECIO_PESC);

        JCoParameterList paramsTable = function.getTableParameterList();

        EjecutarRFC executeRFC = new EjecutarRFC();
        executeRFC.setImports(function, importParams);
        executeRFC.setTable(paramsTable, "STR_PPC", str_ppcs);

        //Exports
        JCoParameterList tables = function.getTableParameterList();
        function.execute(destination);
        JCoTable tblT_Mensaje = tables.getTable(Tablas.T_MENSAJE);

        Metodos metodos = new Metodos();
        List<HashMap<String, Object>> listT_MENSAJE = metodos.ListarObjetos(tblT_Mensaje);

        PrecioPescaMantExports dto = new PrecioPescaMantExports();
        dto.setT_mensaje(listT_MENSAJE);
        dto.setMensaje("OK");

        return dto;
    }
}