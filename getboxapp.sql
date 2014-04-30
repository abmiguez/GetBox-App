DROP TABLE IF EXISTS `BOXTOKENS`;
DROP TABLE IF EXISTS `DROPBOXTOKENS`;
DROP TABLE IF EXISTS `USERS`;

--
-- Estructura de tabla para la tabla `BOXTOKENS`
--

CREATE TABLE IF NOT EXISTS `BOXTOKENS` (
  `ID` int(100) NOT NULL AUTO_INCREMENT,
  `ACCESSTOKEN` varchar(120) COLLATE latin1_spanish_ci NOT NULL,
  `REFRESHTOKEN` varchar(120) COLLATE latin1_spanish_ci NOT NULL,
  `USERNAME` varchar(30) COLLATE latin1_spanish_ci NOT NULL,
  `SPACE` bigint(100) NOT NULL,
  `USERID` varchar(20) COLLATE latin1_spanish_ci NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `USERID` (`USERID`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci AUTO_INCREMENT=2 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `DROPBOXTOKENS`
--

CREATE TABLE IF NOT EXISTS `DROPBOXTOKENS` (
  `ID` int(100) NOT NULL AUTO_INCREMENT,
  `TOKENKEY` varchar(120) COLLATE latin1_spanish_ci NOT NULL,
  `TOKENSECRET` varchar(120) COLLATE latin1_spanish_ci NOT NULL,
  `USERNAME` varchar(30) COLLATE latin1_spanish_ci NOT NULL,
  `SPACE` bigint(100) NOT NULL,
  `USERID` varchar(20) COLLATE latin1_spanish_ci NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `USERID` (`USERID`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci AUTO_INCREMENT=9 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `USERS`
--

CREATE TABLE IF NOT EXISTS `USERS` (
  `NAME` varchar(60) COLLATE latin1_spanish_ci NOT NULL,
  `MAIL` varchar(60) COLLATE latin1_spanish_ci NOT NULL,
  `USERNAME` varchar(20) COLLATE latin1_spanish_ci NOT NULL,
  `PASSWORD` varchar(120) COLLATE latin1_spanish_ci NOT NULL,
  `DEL` int(5) NOT NULL DEFAULT '0',
  PRIMARY KEY (`USERNAME`),
  UNIQUE KEY `MAIL` (`MAIL`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci;