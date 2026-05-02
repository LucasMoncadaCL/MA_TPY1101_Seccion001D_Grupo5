


SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


CREATE SCHEMA IF NOT EXISTS "public";


ALTER SCHEMA "public" OWNER TO "postgres";


CREATE TYPE "public"."individual_condition_enum" AS ENUM (
    'good',
    'damaged_repairable',
    'damaged_no_diagnosis',
    'irreparable'
);


ALTER TYPE "public"."individual_condition_enum" OWNER TO "postgres";


CREATE TYPE "public"."individual_status_enum" AS ENUM (
    'available',
    'loaned',
    'maintenance',
    'damaged',
    'blocked',
    'retired'
);


ALTER TYPE "public"."individual_status_enum" OWNER TO "postgres";


CREATE TYPE "public"."item_type_enum" AS ENUM (
    'consumable',
    'reusable',
    'individual'
);


ALTER TYPE "public"."item_type_enum" OWNER TO "postgres";


CREATE TYPE "public"."loan_status_enum" AS ENUM (
    'pending',
    'in_review',
    'approved',
    'rejected',
    'in_progress',
    'completed',
    'cancelled',
    'overdue'
);


ALTER TYPE "public"."loan_status_enum" OWNER TO "postgres";


CREATE TYPE "public"."return_condition_enum" AS ENUM (
    'good',
    'damaged'
);


ALTER TYPE "public"."return_condition_enum" OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."fn_create_stock_on_implement"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN
    INSERT INTO stock (implement_id) VALUES (NEW.id);
    RETURN NEW;
END;
$$;


ALTER FUNCTION "public"."fn_create_stock_on_implement"() OWNER TO "postgres";

SET default_tablespace = '';

SET default_table_access_method = "heap";


CREATE TABLE IF NOT EXISTS "public"."category" (
    "id" integer NOT NULL,
    "name" character varying(100) NOT NULL,
    "description" character varying(255),
    "active" boolean DEFAULT true NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."category" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."category_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."category_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."category_id_seq" OWNED BY "public"."category"."id";



CREATE TABLE IF NOT EXISTS "public"."flyway_schema_history" (
    "installed_rank" integer NOT NULL,
    "version" character varying(50),
    "description" character varying(200) NOT NULL,
    "type" character varying(20) NOT NULL,
    "script" character varying(1000) NOT NULL,
    "checksum" integer,
    "installed_by" character varying(100) NOT NULL,
    "installed_on" timestamp without time zone DEFAULT "now"() NOT NULL,
    "execution_time" integer NOT NULL,
    "success" boolean NOT NULL
);


ALTER TABLE "public"."flyway_schema_history" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."implement" (
    "id" integer NOT NULL,
    "name" character varying(150) NOT NULL,
    "description" "text",
    "category_id" integer,
    "location_id" integer NOT NULL,
    "item_type" "public"."item_type_enum" DEFAULT 'reusable'::"public"."item_type_enum" NOT NULL,
    "barcode" character varying(100),
    "img_url" "text",
    "active" boolean DEFAULT true NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "observations" character varying(500)
);


ALTER TABLE "public"."implement" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."implement_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."implement_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."implement_id_seq" OWNED BY "public"."implement"."id";



CREATE TABLE IF NOT EXISTS "public"."individual" (
    "id" integer NOT NULL,
    "implement_id" integer NOT NULL,
    "asset_code" character varying(100) NOT NULL,
    "status" "public"."individual_status_enum" DEFAULT 'available'::"public"."individual_status_enum" NOT NULL,
    "condition" "public"."individual_condition_enum" DEFAULT 'good'::"public"."individual_condition_enum" NOT NULL,
    "current_location_id" integer,
    "active" boolean DEFAULT true NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."individual" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."individual_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."individual_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."individual_id_seq" OWNED BY "public"."individual"."id";



CREATE TABLE IF NOT EXISTS "public"."loan" (
    "id" integer NOT NULL,
    "requester_id" integer,
    "external_name" character varying(150),
    "external_contact" character varying(150),
    "room_id" integer,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "scheduled_date" "date" NOT NULL,
    "scheduled_time" time without time zone NOT NULL,
    "due_date" "date",
    "status" "public"."loan_status_enum" DEFAULT 'pending'::"public"."loan_status_enum" NOT NULL,
    "approved_at" timestamp with time zone,
    "review_notes" "text",
    "rejection_reason" "text",
    "cancellation_reason" "text",
    "delivered_at" timestamp with time zone,
    "completed_at" timestamp with time zone,
    CONSTRAINT "chk_loan_requester" CHECK ((("requester_id" IS NOT NULL) OR (("external_name" IS NOT NULL) AND ("external_contact" IS NOT NULL))))
);


ALTER TABLE "public"."loan" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."loan_detail" (
    "id" integer NOT NULL,
    "loan_id" integer NOT NULL,
    "implement_id" integer NOT NULL,
    "requested_quantity" integer NOT NULL,
    "reserved_quantity" integer DEFAULT 0 NOT NULL,
    "delivered_quantity" integer DEFAULT 0 NOT NULL,
    CONSTRAINT "chk_detail_quantities" CHECK ((("reserved_quantity" <= "requested_quantity") AND ("delivered_quantity" <= "reserved_quantity"))),
    CONSTRAINT "loan_detail_delivered_quantity_check" CHECK (("delivered_quantity" >= 0)),
    CONSTRAINT "loan_detail_requested_quantity_check" CHECK (("requested_quantity" > 0)),
    CONSTRAINT "loan_detail_reserved_quantity_check" CHECK (("reserved_quantity" >= 0))
);


ALTER TABLE "public"."loan_detail" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."loan_detail_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."loan_detail_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."loan_detail_id_seq" OWNED BY "public"."loan_detail"."id";



CREATE TABLE IF NOT EXISTS "public"."loan_detail_individual" (
    "id" integer NOT NULL,
    "loan_detail_id" integer NOT NULL,
    "individual_id" integer NOT NULL,
    "return_condition" "public"."return_condition_enum",
    "returned_at" timestamp with time zone
);


ALTER TABLE "public"."loan_detail_individual" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."loan_detail_individual_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."loan_detail_individual_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."loan_detail_individual_id_seq" OWNED BY "public"."loan_detail_individual"."id";



CREATE SEQUENCE IF NOT EXISTS "public"."loan_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."loan_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."loan_id_seq" OWNED BY "public"."loan"."id";



CREATE TABLE IF NOT EXISTS "public"."location" (
    "id" integer NOT NULL,
    "name" character varying(100) NOT NULL,
    "description" character varying(255)
);


ALTER TABLE "public"."location" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."location_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."location_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."location_id_seq" OWNED BY "public"."location"."id";



CREATE TABLE IF NOT EXISTS "public"."role" (
    "id" integer NOT NULL,
    "name" character varying(80) NOT NULL,
    "description" character varying(255)
);


ALTER TABLE "public"."role" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."role_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."role_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."role_id_seq" OWNED BY "public"."role"."id";



CREATE TABLE IF NOT EXISTS "public"."room" (
    "id" integer NOT NULL,
    "name" character varying(100) NOT NULL,
    "description" character varying(255)
);


ALTER TABLE "public"."room" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."room_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."room_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."room_id_seq" OWNED BY "public"."room"."id";



CREATE TABLE IF NOT EXISTS "public"."stock" (
    "id" integer NOT NULL,
    "implement_id" integer NOT NULL,
    "total_stock" integer DEFAULT 0 NOT NULL,
    "min_stock" integer DEFAULT 0 NOT NULL,
    "available" integer DEFAULT 0 NOT NULL,
    "reserved" integer DEFAULT 0 NOT NULL,
    "loaned" integer DEFAULT 0 NOT NULL,
    "damaged" integer DEFAULT 0 NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "chk_stock_invariant" CHECK ((((("available" + "reserved") + "loaned") + "damaged") <= "total_stock")),
    CONSTRAINT "stock_available_check" CHECK (("available" >= 0)),
    CONSTRAINT "stock_damaged_check" CHECK (("damaged" >= 0)),
    CONSTRAINT "stock_loaned_check" CHECK (("loaned" >= 0)),
    CONSTRAINT "stock_min_stock_check" CHECK (("min_stock" >= 0)),
    CONSTRAINT "stock_reserved_check" CHECK (("reserved" >= 0)),
    CONSTRAINT "stock_total_stock_check" CHECK (("total_stock" >= 0))
);


ALTER TABLE "public"."stock" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."stock_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."stock_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."stock_id_seq" OWNED BY "public"."stock"."id";



CREATE TABLE IF NOT EXISTS "public"."user" (
    "id" integer NOT NULL,
    "name" character varying(150) NOT NULL,
    "rut" character varying(12) NOT NULL,
    "email" character varying(150) NOT NULL,
    "password_hash" character varying(255) NOT NULL,
    "role_id" integer NOT NULL,
    "active" boolean DEFAULT true NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."user" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."user_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE "public"."user_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."user_id_seq" OWNED BY "public"."user"."id";



CREATE OR REPLACE VIEW "public"."v_active_loans" AS
 SELECT "lo"."id" AS "loan_id",
    "u"."name" AS "requester_name",
    "u"."rut" AS "requester_rut",
    "lo"."external_name",
    "r"."name" AS "room_name",
    "lo"."scheduled_date",
    "lo"."scheduled_time",
    "lo"."due_date",
    "lo"."status",
    "lo"."created_at",
    (("lo"."due_date" < CURRENT_DATE) AND ("lo"."status" = 'in_progress'::"public"."loan_status_enum")) AS "is_overdue"
   FROM (("public"."loan" "lo"
     LEFT JOIN "public"."user" "u" ON (("u"."id" = "lo"."requester_id")))
     LEFT JOIN "public"."room" "r" ON (("r"."id" = "lo"."room_id")))
  WHERE ("lo"."status" <> ALL (ARRAY['completed'::"public"."loan_status_enum", 'cancelled'::"public"."loan_status_enum", 'rejected'::"public"."loan_status_enum"]));


ALTER VIEW "public"."v_active_loans" OWNER TO "postgres";


CREATE OR REPLACE VIEW "public"."v_stock_summary" AS
 SELECT "i"."id" AS "implement_id",
    "i"."name" AS "implement_name",
    "c"."name" AS "category",
    "l"."name" AS "location",
    "i"."item_type",
    "s"."total_stock",
    "s"."min_stock",
    "s"."available",
    "s"."reserved",
    "s"."loaned",
    "s"."damaged",
        CASE
            WHEN ("s"."loaned" > 0) THEN 'Prestado'::character varying
            ELSE "l"."name"
        END AS "display_location"
   FROM ((("public"."implement" "i"
     JOIN "public"."stock" "s" ON (("s"."implement_id" = "i"."id")))
     LEFT JOIN "public"."category" "c" ON (("c"."id" = "i"."category_id")))
     LEFT JOIN "public"."location" "l" ON (("l"."id" = "i"."location_id")))
  WHERE ("i"."active" = true);


ALTER VIEW "public"."v_stock_summary" OWNER TO "postgres";


ALTER TABLE ONLY "public"."category" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."category_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."implement" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."implement_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."individual" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."individual_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."loan" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."loan_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."loan_detail" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."loan_detail_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."loan_detail_individual" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."loan_detail_individual_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."location" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."location_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."role" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."role_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."room" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."room_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."stock" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."stock_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."user" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."user_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."category"
    ADD CONSTRAINT "category_name_key" UNIQUE ("name");



ALTER TABLE ONLY "public"."category"
    ADD CONSTRAINT "category_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."flyway_schema_history"
    ADD CONSTRAINT "flyway_schema_history_pk" PRIMARY KEY ("installed_rank");



ALTER TABLE ONLY "public"."implement"
    ADD CONSTRAINT "implement_name_key" UNIQUE ("name");



ALTER TABLE ONLY "public"."implement"
    ADD CONSTRAINT "implement_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."individual"
    ADD CONSTRAINT "individual_asset_code_key" UNIQUE ("asset_code");



ALTER TABLE ONLY "public"."individual"
    ADD CONSTRAINT "individual_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."loan_detail_individual"
    ADD CONSTRAINT "loan_detail_individual_loan_detail_id_individual_id_key" UNIQUE ("loan_detail_id", "individual_id");



ALTER TABLE ONLY "public"."loan_detail_individual"
    ADD CONSTRAINT "loan_detail_individual_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."loan_detail"
    ADD CONSTRAINT "loan_detail_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."loan"
    ADD CONSTRAINT "loan_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."location"
    ADD CONSTRAINT "location_name_key" UNIQUE ("name");



ALTER TABLE ONLY "public"."location"
    ADD CONSTRAINT "location_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."role"
    ADD CONSTRAINT "role_name_key" UNIQUE ("name");



ALTER TABLE ONLY "public"."role"
    ADD CONSTRAINT "role_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."room"
    ADD CONSTRAINT "room_name_key" UNIQUE ("name");



ALTER TABLE ONLY "public"."room"
    ADD CONSTRAINT "room_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."stock"
    ADD CONSTRAINT "stock_implement_id_key" UNIQUE ("implement_id");



ALTER TABLE ONLY "public"."stock"
    ADD CONSTRAINT "stock_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user"
    ADD CONSTRAINT "user_email_key" UNIQUE ("email");



ALTER TABLE ONLY "public"."user"
    ADD CONSTRAINT "user_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user"
    ADD CONSTRAINT "user_rut_key" UNIQUE ("rut");



CREATE INDEX "flyway_schema_history_s_idx" ON "public"."flyway_schema_history" USING "btree" ("success");



CREATE INDEX "idx_category_active" ON "public"."category" USING "btree" ("active");



CREATE INDEX "idx_implement_active" ON "public"."implement" USING "btree" ("active");



CREATE INDEX "idx_implement_category" ON "public"."implement" USING "btree" ("category_id");



CREATE INDEX "idx_individual_implement" ON "public"."individual" USING "btree" ("implement_id");



CREATE INDEX "idx_individual_status" ON "public"."individual" USING "btree" ("status");



CREATE INDEX "idx_ldi_individual" ON "public"."loan_detail_individual" USING "btree" ("individual_id");



CREATE INDEX "idx_ldi_loan_detail" ON "public"."loan_detail_individual" USING "btree" ("loan_detail_id");



CREATE INDEX "idx_loan_detail_implement" ON "public"."loan_detail" USING "btree" ("implement_id");



CREATE INDEX "idx_loan_detail_loan" ON "public"."loan_detail" USING "btree" ("loan_id");



CREATE INDEX "idx_loan_due_date" ON "public"."loan" USING "btree" ("due_date") WHERE ("due_date" IS NOT NULL);



CREATE INDEX "idx_loan_requester" ON "public"."loan" USING "btree" ("requester_id");



CREATE INDEX "idx_loan_status" ON "public"."loan" USING "btree" ("status");



CREATE INDEX "idx_stock_implement" ON "public"."stock" USING "btree" ("implement_id");



CREATE INDEX "idx_user_active" ON "public"."user" USING "btree" ("active");



CREATE INDEX "idx_user_rut" ON "public"."user" USING "btree" ("rut");



CREATE OR REPLACE TRIGGER "trg_create_stock_on_implement" AFTER INSERT ON "public"."implement" FOR EACH ROW EXECUTE FUNCTION "public"."fn_create_stock_on_implement"();



ALTER TABLE ONLY "public"."implement"
    ADD CONSTRAINT "implement_category_id_fkey" FOREIGN KEY ("category_id") REFERENCES "public"."category"("id") ON DELETE SET NULL;



ALTER TABLE ONLY "public"."implement"
    ADD CONSTRAINT "implement_location_fk" FOREIGN KEY ("location_id") REFERENCES "public"."location"("id") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."implement"
    ADD CONSTRAINT "implement_location_id_fkey" FOREIGN KEY ("location_id") REFERENCES "public"."location"("id") ON DELETE SET NULL;



ALTER TABLE ONLY "public"."individual"
    ADD CONSTRAINT "individual_current_location_id_fkey" FOREIGN KEY ("current_location_id") REFERENCES "public"."location"("id") ON DELETE SET NULL;



ALTER TABLE ONLY "public"."individual"
    ADD CONSTRAINT "individual_implement_id_fkey" FOREIGN KEY ("implement_id") REFERENCES "public"."implement"("id");



ALTER TABLE ONLY "public"."loan_detail"
    ADD CONSTRAINT "loan_detail_implement_id_fkey" FOREIGN KEY ("implement_id") REFERENCES "public"."implement"("id");



ALTER TABLE ONLY "public"."loan_detail_individual"
    ADD CONSTRAINT "loan_detail_individual_individual_id_fkey" FOREIGN KEY ("individual_id") REFERENCES "public"."individual"("id");



ALTER TABLE ONLY "public"."loan_detail_individual"
    ADD CONSTRAINT "loan_detail_individual_loan_detail_id_fkey" FOREIGN KEY ("loan_detail_id") REFERENCES "public"."loan_detail"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."loan_detail"
    ADD CONSTRAINT "loan_detail_loan_id_fkey" FOREIGN KEY ("loan_id") REFERENCES "public"."loan"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."loan"
    ADD CONSTRAINT "loan_requester_id_fkey" FOREIGN KEY ("requester_id") REFERENCES "public"."user"("id") ON DELETE SET NULL;



ALTER TABLE ONLY "public"."loan"
    ADD CONSTRAINT "loan_room_id_fkey" FOREIGN KEY ("room_id") REFERENCES "public"."room"("id") ON DELETE SET NULL;



ALTER TABLE ONLY "public"."stock"
    ADD CONSTRAINT "stock_implement_id_fkey" FOREIGN KEY ("implement_id") REFERENCES "public"."implement"("id");



ALTER TABLE ONLY "public"."user"
    ADD CONSTRAINT "user_role_id_fkey" FOREIGN KEY ("role_id") REFERENCES "public"."role"("id");



REVOKE USAGE ON SCHEMA "public" FROM PUBLIC;




