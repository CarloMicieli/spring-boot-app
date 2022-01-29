CREATE TABLE public.users
(
    username varchar(25) NOT NULL,
    password varchar(100) NOT NULL,
    is_expired boolean NULL DEFAULT false,
    is_locked boolean NULL DEFAULT false,
    role varchar(50) NULL DEFAULT 'user',
    version integer NOT NULL DEFAULT 0,

    CONSTRAINT "PK_users" PRIMARY KEY (username)
);

CREATE TABLE public.catalog_items
(
    catalog_item_id uuid NOT NULL,
    brand varchar(25) NOT NULL,
    item_number varchar(10) NOT NULL,
    slug varchar(50) NOT NULL,
    scale varchar(10) NOT NULL,
    description varchar(1000) NOT NULL,
    epoch varchar(10) NOT NULL,
    category varchar(100) NOT NULL,
    version integer NOT NULL DEFAULT 0,

    CONSTRAINT "PK_catalog_items" PRIMARY KEY(catalog_item_id)
);

CREATE TABLE public.rolling_stocks
(
    rolling_stock_id uuid NOT NULL,
    catalog_item_id uuid NOT NULL,
    railway varchar(50) NOT NULL,
    type_name varchar(25) NULL,
    road_number varchar(25) NULL,
    category varchar(100) NULL,
    version integer NOT NULL DEFAULT 0,

    CONSTRAINT "PK_rolling_stocks" PRIMARY KEY(rolling_stock_id),
    CONSTRAINT "FK_rolling_stocks_catalog_items" FOREIGN KEY (catalog_item_id)
         REFERENCES public.catalog_items (catalog_item_id) MATCH SIMPLE
         ON UPDATE NO ACTION
         ON DELETE NO ACTION
);
