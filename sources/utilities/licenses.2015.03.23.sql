
--
-- Name: piwik_report; Type: TABLE; Schema: public; Owner: dspace; Tablespace: 
--

CREATE TABLE piwik_report (
	report_id integer NOT NULL,
    eperson_id integer NOT NULL,
    item_id integer NOT NULL  
);


ALTER TABLE public.piwik_report OWNER TO dspace;

--
-- Name: piwik_report_report_id_seq; Type: SEQUENCE; Schema: public; Owner: dspace
--

CREATE SEQUENCE piwik_report_report_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.piwik_report_report_id_seq OWNER TO dspace;

--
-- Name: piwik_report_report_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: dspace
--

ALTER SEQUENCE piwik_report_report_id_seq OWNED BY piwik_report.report_id;


--
-- Name: report_id; Type: DEFAULT; Schema: public; Owner: dspace
--

ALTER TABLE ONLY piwik_report ALTER COLUMN report_id SET DEFAULT nextval('piwik_report_report_id_seq'::regclass);

--
-- Name: piwik_report_pkey; Type: CONSTRAINT; Schema: public; Owner: dspace; Tablespace: 
--

ALTER TABLE ONLY piwik_report
    ADD CONSTRAINT piwik_report_pkey PRIMARY KEY (report_id);

--
-- Name: piwik_report_eperson_id_item_id_key; Type: INDEX; Schema: public; Owner: dspace; Tablespace: 
--

CREATE UNIQUE INDEX piwik_report_eperson_id_item_id_key ON piwik_report(eperson_id, item_id);
