package com.porto.corporativo.licitacoes.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.json.XML;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.porto.arquitetura.security.bean.ISubject;
import com.porto.corporativo.licitacoes.constant.Constantes;
import com.porto.corporativo.licitacoes.integration.interfaces.SusepLicitarIntegratorLocal;
import com.porto.corporativo.licitacoes.rest.exception.LicitarException;
import com.porto.infra.util.LogManager;

// TODO: Auto-generated Javadoc
/**
 * Classe utilitaria para conversoes entre tipos.
 */
public final class ParserUtil {

	/** The Constant LOGGER. */
	private static final LogManager LOGGER = LogManager.getLog(ParserUtil.class);

	/** The regex namespace. */
	private static final String REGEX_NAMESPACE = "(<\\/?)[a-zA-Z0-9\\-]+:";

	/** The replace namespace. */
	private static final String REPLACE_NAMESPACE = "$1";

	@Inject
	private static SusepLicitarIntegratorLocal susepLicitarIntegration;

	/**
	 * Instantiates a new parser util.
	 */
	private ParserUtil() {
		// Empty Constructor
	}

	/**
	 * Converter para json.
	 * 
	 * @param objeto
	 *            the objeto
	 * @return the string
	 * @throws LicitarException
	 *             the licitar exception
	 */
	public static String converterParaJson(final Object objeto) throws LicitarException {
		final ObjectMapper mapper = new ObjectMapper();
		String json = StringUtils.EMPTY;
		try {
			json = mapper.writeValueAsString(objeto);
		} catch (final JsonProcessingException e) {
			LOGGER.error(e);
			throw new LicitarException(e, e.getMessage());
		}
		return json;
	}

	/**
	 * Convert value.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param fromValue
	 *            the from value
	 * @param toValueType
	 *            the to value type
	 * @return the t
	 */
	public static <T> T convertValue(final Object fromValue, final Class<T> toValueType) {

		T valueConverted = null;

		if (fromValue != null) {
			final ObjectMapper mapper = new ObjectMapper();

			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

			valueConverted = mapper.convertValue(fromValue, toValueType);

		}

		return valueConverted;

	}

	/**
	 * Convert values.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param fromValues
	 *            the from values
	 * @param toValueType
	 *            the to value type
	 * @return the list
	 */
	public static <T> List<T> convertValues(final Object fromValues, final Class<T> toValueType) {

		List<T> valuesConverted = null;

		if (fromValues != null) {

			final ObjectMapper mapper = new ObjectMapper();

			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

			valuesConverted = mapper.convertValue(fromValues,
					TypeFactory.defaultInstance().constructCollectionType(List.class, toValueType));

		}

		return valuesConverted;

	}

	/**
	 * Converter xml formatado.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param conteudo
	 *            the conteudo
	 * @param toValueType
	 *            the to value type
	 * @return the t
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static <T> T converterXMLFormatado(final String conteudo, final Class<T> toValueType)
			throws JsonParseException, JsonMappingException, IOException {

		T value = null;

		if (StringUtils.isNotEmpty(conteudo)) {

			final ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			final JSONObject jsonObject = XML.toJSONObject(conteudo);
			value = objectMapper.readValue(jsonObject.toString(), toValueType);

		}

		return value;

	}

	/**
	 * Formatar response.
	 * 
	 * @param conteudo
	 *            the conteudo
	 * @param expressao
	 *            the expressao
	 * @return the string
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	public static String formatarResponse(final String conteudo, final String expressao)
			throws UnsupportedEncodingException {

		String conteudoFormatado = null;

		if (StringUtils.isNotEmpty(conteudo)) {

			conteudoFormatado = conteudo.replaceAll(REGEX_NAMESPACE, REPLACE_NAMESPACE).replaceAll(
					"xmlns.*?(\"|\').*?(\"|\')", "");

			final Pattern padrao = Pattern.compile(expressao);

			final Matcher matcher = padrao.matcher(conteudoFormatado);

			if (matcher.find()) {

				conteudoFormatado = matcher.group(1);
			}

		}

		return conteudoFormatado;

	}

	/**
	 * Inserir dados auditoria.
	 * 
	 * @param object
	 *            the object
	 * @param codigoTipoAlteracao
	 *            the codigo tipo alteracao
	 * @param subjectBean
	 *            the subject bean
	 * @throws Exception
	 *             the exception
	 */
	public static void inserirDadosAuditoria(final Object object, final String codigoTipoAlteracao,
			final ISubject subjectBean) throws Exception {

		if (object != null && subjectBean != null) {

			LOGGER.info("USER ID: " + subjectBean.getUserId());
			LOGGER.info("USER TYPE: " + subjectBean.getUserType());
			LOGGER.info("EMPLYER: " + subjectBean.getEmployer());
			LOGGER.info("PORTAL: " + subjectBean.getPortal());

			final String matricula = subjectBean.getUserId();
			final Long numeroMatriculaAtualizacao = Long.valueOf(matricula.substring(3, matricula.length()));
			final String tipoUsuario = subjectBean.getUserType();
			final Long codigoEmpresa = Long.valueOf(subjectBean.getEmployer());
			final Integer portal = subjectBean.getPortal();

			final Class<? extends Object> c = object.getClass();

			final Method setNumeroMatriculaAtualizacao = getMethod(c, "setNumeroMatriculaAtualizacao", Long.class);
			final Method setDataAlteracao = getMethod(c, "setDataAlteracao", Date.class);
			final Method setCodigoTipoUsuarioAtualizacao = getMethod(c, "setCodigoTipoUsuarioAtualizacao", String.class);
			final Method setCodigoEmpresaAtualizacao = getMethod(c, "setCodigoEmpresaAtualizacao", Long.class);
			final Method setCodigoTipoAlteracao = getMethod(c, "setCodigoTipoAlteracao", String.class);
			final Method setCodigoPortalAtualizacao = getMethod(c, "setCodigoPortalAtualizacao", Long.class);
			final Method setCodigoPortalAtualizacaoInteger = getMethod(c, "setCodigoPortalAtualizacao", Integer.class);
			final Method setCodigoUsuarioAtualizacao = getMethod(c, "setCodigoUsuarioAtualizacao", String.class);

			if (setNumeroMatriculaAtualizacao != null) {
				setNumeroMatriculaAtualizacao.invoke(object, numeroMatriculaAtualizacao);
			}
			if (setDataAlteracao != null) {
				setDataAlteracao.invoke(object, new Date());
			}
			if (setCodigoTipoUsuarioAtualizacao != null) {
				setCodigoTipoUsuarioAtualizacao.invoke(object, tipoUsuario);
			}
			if (setCodigoEmpresaAtualizacao != null) {
				setCodigoEmpresaAtualizacao.invoke(object, codigoEmpresa);
			}
			if (setCodigoTipoAlteracao != null) {
				setCodigoTipoAlteracao.invoke(object, codigoTipoAlteracao);
			}
			if (setCodigoPortalAtualizacao != null) {
				setCodigoPortalAtualizacao.invoke(object, (long) portal);
			}
			if (setCodigoPortalAtualizacaoInteger != null) {
				setCodigoPortalAtualizacaoInteger.invoke(object, portal);
			}
			if (setCodigoUsuarioAtualizacao != null) {
				setCodigoUsuarioAtualizacao.invoke(object, matricula);
			}
		}
	}

	/**
	 * Inserir dados auditoria.
	 * 
	 * @param object
	 *            the object
	 * @param codigoTipoAlteracao
	 *            the codigo tipo alteracao
	 * @param subjectBean
	 *            the subject bean
	 * @param susep
	 *            the susep
	 * @throws Exception
	 *             the exception
	 */
	public static void inserirDadosAuditoria(final Object object, final String codigoTipoAlteracao,
			final ISubject subjectBean, final String susep) throws Exception {

		if (object != null && subjectBean != null) {

			LOGGER.info("USER ID: " + subjectBean.getUserId());
			LOGGER.info("USER TYPE: " + subjectBean.getUserType());
			LOGGER.info("EMPLYER: " + subjectBean.getEmployer());
			LOGGER.info("PORTAL: " + subjectBean.getPortal());

			final String matricula = subjectBean.getUserId();
			final Long numeroMatriculaAtualizacao = Long.valueOf(matricula.substring(3, matricula.length()));
			final String tipoUsuario = subjectBean.getUserType();
			final Long codigoEmpresa = Long.valueOf(subjectBean.getEmployer());
			final Integer portal = subjectBean.getPortal();

			final Class<? extends Object> c = object.getClass();

			final Method setNumeroMatriculaAtualizacao = getMethod(c, "setNumeroMatriculaAtualizacao", Long.class);
			final Method setDataAlteracao = getMethod(c, "setDataAlteracao", Date.class);
			final Method setCodigoTipoUsuarioAtualizacao = getMethod(c, "setCodigoTipoUsuarioAtualizacao", String.class);
			final Method setCodigoEmpresaAtualizacao = getMethod(c, "setCodigoEmpresaAtualizacao", Long.class);
			final Method setCodigoTipoAlteracao = getMethod(c, "setCodigoTipoAlteracao", String.class);
			final Method setCodigoPortalAtualizacao = getMethod(c, "setCodigoPortalAtualizacao", Long.class);
			final Method setCodigoPortalAtualizacaoInteger = getMethod(c, "setCodigoPortalAtualizacao", Integer.class);
			final Method setCodigoUsuarioAtualizacao = getMethod(c, "setCodigoUsuarioAtualizacao", String.class);

			if (setNumeroMatriculaAtualizacao != null) {
				setNumeroMatriculaAtualizacao.invoke(object, numeroMatriculaAtualizacao);
			}
			if (setDataAlteracao != null) {
				setDataAlteracao.invoke(object, new Date());
			}
			if (setCodigoTipoUsuarioAtualizacao != null) {
				setCodigoTipoUsuarioAtualizacao.invoke(object, tipoUsuario);
			}
			if (setCodigoEmpresaAtualizacao != null) {
				setCodigoEmpresaAtualizacao.invoke(object, codigoEmpresa);
			}
			if (setCodigoTipoAlteracao != null) {
				setCodigoTipoAlteracao.invoke(object, codigoTipoAlteracao);
			}
			if (setCodigoPortalAtualizacao != null) {
				setCodigoPortalAtualizacao.invoke(object, (long) portal);
			}
			if (setCodigoPortalAtualizacaoInteger != null) {
				setCodigoPortalAtualizacaoInteger.invoke(object, portal);
			}
			if (setCodigoUsuarioAtualizacao != null) {
				if (subjectBean.getPortal() == Constantes.CODIGO_PORTAL_COL) {

					setCodigoUsuarioAtualizacao.invoke(object, susep);
				} else {
					setCodigoUsuarioAtualizacao.invoke(object, matricula);
				}

			}
		}
	}

	/**
	 * Gets the method.
	 * 
	 * @param c
	 *            the c
	 * @param name
	 *            the name
	 * @param parameterTypes
	 *            the parameter types
	 * @return the method
	 */
	private static Method getMethod(final Class<? extends Object> c, final String name,
			final Class<?>... parameterTypes) {
		Method method = null;

		try {
			method = c.getMethod(name, parameterTypes);
		} catch (final NoSuchMethodException e) {
		}

		return method;
	}

	/**
	 * Date to calendar.
	 * 
	 * @param date
	 *            the date
	 * @return the calendar
	 */
	public static Calendar dateToCalendar(final Date date) {

		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;

	}

}
