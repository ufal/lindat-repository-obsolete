
--
-- Name: download_token; Type: TABLE; Schema: public; Owner: dspace; Tablespace: 
--

CREATE TABLE download_token (
    token character(32) NOT NULL,
    bitstream_id integer NOT NULL,
    eperson_id integer NOT NULL,
    date_created date NOT NULL
);


ALTER TABLE public.download_token OWNER TO dspace;

--
-- Name: token_primary_key; Type: CONSTRAINT; Schema: public; Owner: dspace; Tablespace: 
--

ALTER TABLE ONLY download_token
    ADD CONSTRAINT token_primary_key PRIMARY KEY (token);