/**
 * Copyright [2009] [NIC Labs]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the 	License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or 
 * agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 **/


package cl.nic.dte.extension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.SAXException;

import cl.nic.dte.VerifyResult;
import cl.nic.dte.util.Utilities;
import cl.nic.dte.util.XMLUtil;
import cl.sii.siiDte.consumofolios.ConsumoFoliosDocument;

public class ConsumoFoliosDocumentExtensionHandler {

	public static byte[] getBytes(ConsumoFoliosDocument dte) throws IOException {

		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dte.save(out, opts);
		return out.toByteArray();

	}

	public static VerifyResult verifyXML(ConsumoFoliosDocument dte) {
		return XMLUtil.verifyXML(dte);

	}

	public static VerifyResult verifySignature(ConsumoFoliosDocument dte) {
		// Ojo, verifica la firma usando el keyValue, pero no verifica que el
		// value corresponda al certificado incluido, ni la validez del
		// certificado.

		cl.sii.siiDte.dsig.SignatureType sign = dte.getConsumoFolios()
				.getSignature();

		// Find Signature element
		if (sign == null || sign.isNil()) {
			return new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_NOTFOUND"));
		}

		return (XMLUtil.verifySignature(sign.getDomNode(), dte
				.getConsumoFolios().getDocumentoConsumoFolios().getCaratula()
				.getTmstFirmaEnv().getTime()));
	}

	public static byte[] sign(ConsumoFoliosDocument dte, PrivateKey pKey,
			X509Certificate cert) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, KeyException, MarshalException,
			XMLSignatureException, SAXException, IOException,
			ParserConfigurationException, XmlException {

		String uri = "#"+dte.getConsumoFolios().getDocumentoConsumoFolios().getID();
		// Segun el esquema no estoy obligado a este formato de fecha, pero
		// prefiero

		XmlDateTime now = XmlDateTime.Factory
				.newValue(Utilities.fechaHoraFormat.format(new Date()));

		dte.getConsumoFolios().getDocumentoConsumoFolios().getCaratula()
				.xsetTmstFirmaEnv(now);

		XMLUtil.signEmbeded(dte.getConsumoFolios().getDomNode(), uri, pKey,
				cert);

		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dte.save(out, opts);

		return out.toByteArray();
	}

	public static X509Certificate getCertificate(ConsumoFoliosDocument dte) {
		return XMLUtil.getCertificate(dte.getConsumoFolios().getSignature());
	}

}
