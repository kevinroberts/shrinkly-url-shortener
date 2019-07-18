--
-- PostgreSQL database dump
--

-- Dumped from database version 11.1
-- Dumped by pg_dump version 11.1

-- Started on 2019-02-25 22:33:31 CST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 196 (class 1259 OID 17372)
-- Name: app_abuse; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_abuse (
    id bigint NOT NULL,
    short_url character varying(120) NOT NULL,
    date_added timestamp without time zone DEFAULT now() NOT NULL,
    user_id bigint,
    full_url character varying(2000),
    ip_address character varying(120)
);


--
-- TOC entry 197 (class 1259 OID 17379)
-- Name: app_abuse_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_abuse_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3288 (class 0 OID 0)
-- Dependencies: 197
-- Name: app_abuse_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_abuse_id_seq OWNED BY public.app_abuse.id;


--
-- TOC entry 198 (class 1259 OID 17381)
-- Name: app_password_reset_token; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_password_reset_token (
    token character varying(120) NOT NULL,
    user_id bigint NOT NULL,
    id bigint NOT NULL,
    expiry_date timestamp without time zone NOT NULL
);


--
-- TOC entry 199 (class 1259 OID 17384)
-- Name: app_password_reset_token_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_password_reset_token_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3289 (class 0 OID 0)
-- Dependencies: 199
-- Name: app_password_reset_token_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_password_reset_token_id_seq OWNED BY public.app_password_reset_token.id;


--
-- TOC entry 200 (class 1259 OID 17386)
-- Name: app_privileges; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_privileges (
    name character varying(30) NOT NULL,
    id bigint NOT NULL
);


--
-- TOC entry 201 (class 1259 OID 17389)
-- Name: app_privileges_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_privileges_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3290 (class 0 OID 0)
-- Dependencies: 201
-- Name: app_privileges_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_privileges_id_seq OWNED BY public.app_privileges.id;


--
-- TOC entry 202 (class 1259 OID 17391)
-- Name: app_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_role (
    name character varying(30) NOT NULL,
    id bigint NOT NULL
);


--
-- TOC entry 203 (class 1259 OID 17394)
-- Name: app_role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3291 (class 0 OID 0)
-- Dependencies: 203
-- Name: app_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_role_id_seq OWNED BY public.app_role.id;


--
-- TOC entry 204 (class 1259 OID 17396)
-- Name: app_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_user (
    user_name character varying(36) NOT NULL,
    password character varying(128) NOT NULL,
    date_added timestamp without time zone DEFAULT now() NOT NULL,
    last_login timestamp without time zone,
    user_email character varying(254) NOT NULL,
    profile_image character varying(1024),
    enabled boolean DEFAULT false NOT NULL,
    user_id bigint NOT NULL,
    is_using_2fa boolean DEFAULT false NOT NULL,
    credentials_non_expired boolean DEFAULT true NOT NULL,
    account_non_expired boolean DEFAULT true NOT NULL,
    account_non_locked boolean DEFAULT false NOT NULL,
    secret character varying(36)
);


--
-- TOC entry 205 (class 1259 OID 17408)
-- Name: app_user_short_urls; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_user_short_urls (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    short_url character varying(120) NOT NULL,
    full_url character varying(2000) NOT NULL,
    date_added timestamp without time zone DEFAULT now() NOT NULL,
    is_custom boolean DEFAULT false NOT NULL,
    expiration_date timestamp without time zone
);


--
-- TOC entry 206 (class 1259 OID 17416)
-- Name: app_user_short_urls_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_user_short_urls_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3292 (class 0 OID 0)
-- Dependencies: 206
-- Name: app_user_short_urls_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_user_short_urls_id_seq OWNED BY public.app_user_short_urls.id;


--
-- TOC entry 207 (class 1259 OID 17418)
-- Name: app_user_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_user_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3293 (class 0 OID 0)
-- Dependencies: 207
-- Name: app_user_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_user_user_id_seq OWNED BY public.app_user.user_id;


--
-- TOC entry 208 (class 1259 OID 17420)
-- Name: app_verification_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_verification_tokens (
    token character varying(128) NOT NULL,
    user_id bigint NOT NULL,
    expiry_date timestamp without time zone,
    id bigint NOT NULL
);


--
-- TOC entry 209 (class 1259 OID 17423)
-- Name: app_verification_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_verification_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3294 (class 0 OID 0)
-- Dependencies: 209
-- Name: app_verification_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_verification_tokens_id_seq OWNED BY public.app_verification_tokens.id;


--
-- TOC entry 210 (class 1259 OID 17425)
-- Name: persistent_logins; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.persistent_logins (
    username character varying(64) NOT NULL,
    series character varying(64) NOT NULL,
    token character varying(64) NOT NULL,
    last_used timestamp without time zone NOT NULL
);


--
-- TOC entry 211 (class 1259 OID 17428)
-- Name: roles_privileges; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles_privileges (
    role_id bigint NOT NULL,
    privilege_id bigint NOT NULL,
    id bigint NOT NULL
);


--
-- TOC entry 212 (class 1259 OID 17431)
-- Name: roles_privileges_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.roles_privileges_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3295 (class 0 OID 0)
-- Dependencies: 212
-- Name: roles_privileges_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.roles_privileges_id_seq OWNED BY public.roles_privileges.id;


--
-- TOC entry 213 (class 1259 OID 17433)
-- Name: short_url_analytics; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.short_url_analytics (
    id bigint NOT NULL,
    short_url character varying(120) NOT NULL,
    browser character varying(255),
    ip_address character varying(120) NOT NULL,
    operating_system character varying(120),
    country_alpha_two character varying(3),
    us_state_ansi character varying(3),
    referrer character varying(300),
    continent_name character varying(30),
    browser_type character varying(40),
    browser_version character varying(20),
    operating_system_version character varying(15),
    device_type character varying(20),
    expiry_date timestamp without time zone,
    date_added timestamp without time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 214 (class 1259 OID 17439)
-- Name: short_url_analytics_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.short_url_analytics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3296 (class 0 OID 0)
-- Dependencies: 214
-- Name: short_url_analytics_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.short_url_analytics_id_seq OWNED BY public.short_url_analytics.id;


--
-- TOC entry 215 (class 1259 OID 17441)
-- Name: user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_roles (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    id bigint NOT NULL
);


--
-- TOC entry 216 (class 1259 OID 17444)
-- Name: user_roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3297 (class 0 OID 0)
-- Dependencies: 216
-- Name: user_roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_roles_id_seq OWNED BY public.user_roles.id;


--
-- TOC entry 3100 (class 2604 OID 17446)
-- Name: app_abuse id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_abuse ALTER COLUMN id SET DEFAULT nextval('public.app_abuse_id_seq'::regclass);


--
-- TOC entry 3101 (class 2604 OID 17447)
-- Name: app_password_reset_token id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_password_reset_token ALTER COLUMN id SET DEFAULT nextval('public.app_password_reset_token_id_seq'::regclass);


--
-- TOC entry 3102 (class 2604 OID 17448)
-- Name: app_privileges id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_privileges ALTER COLUMN id SET DEFAULT nextval('public.app_privileges_id_seq'::regclass);


--
-- TOC entry 3103 (class 2604 OID 17449)
-- Name: app_role id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_role ALTER COLUMN id SET DEFAULT nextval('public.app_role_id_seq'::regclass);


--
-- TOC entry 3110 (class 2604 OID 17450)
-- Name: app_user user_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user ALTER COLUMN user_id SET DEFAULT nextval('public.app_user_user_id_seq'::regclass);


--
-- TOC entry 3113 (class 2604 OID 17451)
-- Name: app_user_short_urls id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user_short_urls ALTER COLUMN id SET DEFAULT nextval('public.app_user_short_urls_id_seq'::regclass);


--
-- TOC entry 3114 (class 2604 OID 17452)
-- Name: app_verification_tokens id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_verification_tokens ALTER COLUMN id SET DEFAULT nextval('public.app_verification_tokens_id_seq'::regclass);


--
-- TOC entry 3115 (class 2604 OID 17453)
-- Name: roles_privileges id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles_privileges ALTER COLUMN id SET DEFAULT nextval('public.roles_privileges_id_seq'::regclass);


--
-- TOC entry 3116 (class 2604 OID 17454)
-- Name: short_url_analytics id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.short_url_analytics ALTER COLUMN id SET DEFAULT nextval('public.short_url_analytics_id_seq'::regclass);


--
-- TOC entry 3118 (class 2604 OID 17455)
-- Name: user_roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles ALTER COLUMN id SET DEFAULT nextval('public.user_roles_id_seq'::regclass);


--
-- TOC entry 3120 (class 2606 OID 17457)
-- Name: app_abuse app_abuse_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_abuse
    ADD CONSTRAINT app_abuse_pkey PRIMARY KEY (id);


--
-- TOC entry 3122 (class 2606 OID 17459)
-- Name: app_password_reset_token app_password_reset_token_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_password_reset_token
    ADD CONSTRAINT app_password_reset_token_pkey PRIMARY KEY (id);


--
-- TOC entry 3124 (class 2606 OID 17461)
-- Name: app_privileges app_privileges_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_privileges
    ADD CONSTRAINT app_privileges_pkey PRIMARY KEY (id);


--
-- TOC entry 3128 (class 2606 OID 17463)
-- Name: app_role app_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_role
    ADD CONSTRAINT app_role_pkey PRIMARY KEY (id);


--
-- TOC entry 3130 (class 2606 OID 17465)
-- Name: app_role app_role_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_role
    ADD CONSTRAINT app_role_uk UNIQUE (name);


--
-- TOC entry 3132 (class 2606 OID 17467)
-- Name: app_user app_user_email_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_email_uk UNIQUE (user_email);


--
-- TOC entry 3135 (class 2606 OID 17469)
-- Name: app_user app_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3139 (class 2606 OID 17471)
-- Name: app_user_short_urls app_user_short_urls_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user_short_urls
    ADD CONSTRAINT app_user_short_urls_pkey PRIMARY KEY (id);


--
-- TOC entry 3137 (class 2606 OID 17473)
-- Name: app_user app_user_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_uk UNIQUE (user_name);


--
-- TOC entry 3142 (class 2606 OID 17475)
-- Name: app_verification_tokens app_verification_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_verification_tokens
    ADD CONSTRAINT app_verification_tokens_pkey PRIMARY KEY (id);


--
-- TOC entry 3146 (class 2606 OID 17477)
-- Name: roles_privileges roles_privileges_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles_privileges
    ADD CONSTRAINT roles_privileges_pkey PRIMARY KEY (id);


--
-- TOC entry 3144 (class 2606 OID 17479)
-- Name: persistent_logins series_role_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.persistent_logins
    ADD CONSTRAINT series_role_pk PRIMARY KEY (series);


--
-- TOC entry 3149 (class 2606 OID 17481)
-- Name: short_url_analytics short_url_analytics_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.short_url_analytics
    ADD CONSTRAINT short_url_analytics_pkey PRIMARY KEY (id);


--
-- TOC entry 3126 (class 2606 OID 17483)
-- Name: app_privileges unique_priv_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_privileges
    ADD CONSTRAINT unique_priv_name UNIQUE (name);


--
-- TOC entry 3151 (class 2606 OID 17485)
-- Name: user_roles user_role_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_role_uk UNIQUE (user_id, role_id);


--
-- TOC entry 3153 (class 2606 OID 17487)
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (id);


--
-- TOC entry 3133 (class 1259 OID 17488)
-- Name: app_user_name_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_user_name_index ON public.app_user USING btree (user_name varchar_ops, user_email varchar_ops);


--
-- TOC entry 3147 (class 1259 OID 17530)
-- Name: short_url_analytics_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX short_url_analytics_index ON public.short_url_analytics USING btree (short_url varchar_ops);


--
-- TOC entry 3140 (class 1259 OID 24796)
-- Name: user_short_url_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_short_url_index ON public.app_user_short_urls USING btree (short_url varchar_ops);


--
-- TOC entry 3158 (class 2606 OID 17489)
-- Name: roles_privileges FK_PRIVILEDGE; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles_privileges
    ADD CONSTRAINT "FK_PRIVILEDGE" FOREIGN KEY (privilege_id) REFERENCES public.app_privileges(id);


--
-- TOC entry 3159 (class 2606 OID 17494)
-- Name: roles_privileges FK_ROLE; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles_privileges
    ADD CONSTRAINT "FK_ROLE" FOREIGN KEY (role_id) REFERENCES public.app_role(id);


--
-- TOC entry 3160 (class 2606 OID 17499)
-- Name: user_roles FK_ROLE_ID; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT "FK_ROLE_ID" FOREIGN KEY (role_id) REFERENCES public.app_role(id);


--
-- TOC entry 3157 (class 2606 OID 17504)
-- Name: app_verification_tokens FK_VERIFY_USER; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_verification_tokens
    ADD CONSTRAINT "FK_VERIFY_USER" FOREIGN KEY (user_id) REFERENCES public.app_user(user_id);


--
-- TOC entry 3154 (class 2606 OID 17509)
-- Name: app_abuse fk_app_abuse_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_abuse
    ADD CONSTRAINT fk_app_abuse_user FOREIGN KEY (user_id) REFERENCES public.app_user(user_id);


--
-- TOC entry 3156 (class 2606 OID 17514)
-- Name: app_user_short_urls fk_app_user_id_short_url; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user_short_urls
    ADD CONSTRAINT fk_app_user_id_short_url FOREIGN KEY (user_id) REFERENCES public.app_user(user_id);


--
-- TOC entry 3155 (class 2606 OID 17519)
-- Name: app_password_reset_token fk_password_reset_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_password_reset_token
    ADD CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES public.app_user(user_id);


--
-- TOC entry 3161 (class 2606 OID 17524)
-- Name: user_roles user_to_user_role_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_to_user_role_fk FOREIGN KEY (user_id) REFERENCES public.app_user(user_id);


-- Completed on 2019-02-25 22:33:31 CST

--
-- PostgreSQL database dump complete
--

