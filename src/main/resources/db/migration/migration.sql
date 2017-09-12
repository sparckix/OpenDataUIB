CREATE TABLE Usuario (
	idusu 	smallint unsigned not null auto_increment,
	nombre	varchar(30) not null,
	clave		varchar(255) not null,
	primary key (idusu)
) engine = innodb;

CREATE TABLE Consulta_Favorita (
	idfav		smallint unsigned not null auto_increment,
	consulta	varchar(255),
	primary key (idfav)
) engine = innodb;

CREATE TABLE Favoritas_Usuario (
	idusu 	smallint unsigned not null references Usuario (idusu),
	idfav		smallint unsigned not null references Consulta_Favorita (idfav),
	primary key (idusu, idfav)
) engine = innodb;
CREATE TABLE Consulta (
	idcons 	smallint unsigned not null auto_increment,
	idusu		smallint unsigned not null references Usuario (idusu),
	idfav		smallint unsigned not null references Consulta_Favorita (idfav),
	fecha		timestamp not null default current_timestamp,
	primary key (idcons)
) engine = innodb;