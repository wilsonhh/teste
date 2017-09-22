package com.porto.corporativo.licitacoes.integration;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import com.porto.corporativo.licitacoes.constant.Constantes;
import com.porto.corporativo.licitacoes.enums.MetodosHTTPEnum;
import com.porto.corporativo.licitacoes.integration.domain.BuscaListaFeriadosResponse;
import com.porto.corporativo.licitacoes.integration.interfaces.ConsultaFeriadosIntegratorLocal;
import com.porto.corporativo.licitacoes.util.ParserUtil;
import com.porto.corporativo.licitacoes.util.WebServiceCaller;

/**
 * The Class ConsultaFeriadoIntegration.
 */
@Stateless
public class ConsultaFeriadoIntegration implements ConsultaFeriadosIntegratorLocal {

	private static final String BUSCA_FERIADOS_REGEX = "<return>(.*)</return>";
	
	private static final String DATA = "{{data}}";
	private static final String CEP = "{{cep}}";
	private static final String BUSCA_FERIADOS = 
			"<soapenv:Envelope " +
			 "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
			 "xmlns:ws=\"http://ws.wsservicoscorp.corporativo.porto.com/\">" +
				"<soapenv:Header/>" +
				"<soapenv:Body>" +
					"<ws:buscaListaFeriados>" +
						"<arg0>{{data}}</arg0>" +
						"<arg1>{{cep}}</arg1>" +
					"</ws:buscaListaFeriados>" +
				"</soapenv:Body>" +
			"</soapenv:Envelope>";
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public BuscaListaFeriadosResponse buscaFeriados(final Date date) throws Exception {
		
		String request = BUSCA_FERIADOS
			.replace(CEP, Constantes.CONSULTA_FERIADOS_CEP)
			.replace(DATA, SDF.format(date));
		
		Map<String, String> parametros = new HashMap<String, String>();
		parametros.put("Content-Type", "text/xml;charset=UTF-8");
		
		String response = WebServiceCaller.chamaWebService(Constantes.CONSULTA_FERIADOS_WEBSERVICE_ENDPOINT, request, parametros,
				MetodosHTTPEnum.POST.toString(), Constantes.CONSULTA_FERIADOS_USUARIO,
				Constantes.CONSULTA_FERIADOS_SENHA,
				StandardCharsets.UTF_8);

		response = ParserUtil.formatarResponse(response, BUSCA_FERIADOS_REGEX);

		return ParserUtil.converterXMLFormatado(response, BuscaListaFeriadosResponse.class);
	}

}
