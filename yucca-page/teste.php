<html>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<link rel="stylesheet" href="https://netdna.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
<link rel="stylesheet" href="teste.css">
<link href="https://fonts.googleapis.com/css?family=Courgette|Istok+Web" rel="stylesheet">
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">

<script language="javascript">
    function InfoPagSementes() {
		if (document.getElementById('pagSementes_sim').checked) {
		document.getElementById('campos').style.display = "";
		document.getElementById('camposQtd').style.display = "none";
		// document.getElementById('textfield').focus();
		}  else {
		document.getElementById('camposQtd').style.display = "";
		document.getElementById('campos').style.display = "none";
		// document.getElementById('textfield').focus();
		}
	}
	  
	  
	function InfoMaxPagSementes() {
		if (document.getElementById('maxPagSementes_sim').checked) {
			document.getElementById('camposMaxQtd').style.display = "";
			document.getElementById('helpCamposMaxQtd').style.display = "";
			// document.getElementById('textfield').focus();
		} else{
			document.getElementById('camposMaxQtd').style.display = "none";
			document.getElementById('helpCamposMaxQtd').style.display = "none";
		}
	} 
	
	function showonlyonev2(thechosenone) {
		var newboxes = document.getElementsByTagName("div");
		for(var x=0; x<newboxes.length; x++) {
			name = newboxes[x].getAttribute("class");
			if (name == 'newboxes-2') {
				if (newboxes[x].id == thechosenone) {
					if (newboxes[x].style.display == 'block') {
						newboxes[x].style.display = 'none';
					}
					else {
						newboxes[x].style.display = 'block';
					}
				}else {
					newboxes[x].style.display = 'none';
				}
			}
		}
	}
</script>

<?php if (isset($_POST['button'])) {
	$name = 'content.collect';
	$conteudo = $_POST['conteudo'];
	$file = fopen($name, 'w');
	fwrite($file, $conteudo);
	fclose($file);
	
	$name = 'genre.collect';
	$genero = $_POST['genero'];
	$file = fopen($name, 'w');
	fwrite($file, $genero);
	fclose($file);
	
	$name = 'pagSementes.collect';
	$pagSementes = $_POST['pagSementes'];
	$file = fopen($name, 'w');
	$pagSementes = $pagSementes."\n";
	printf('pags');
	printf($pagSementes);
	if($pagSementes == 0){
		fwrite($file, $pagSementes);
		$qtdPag = $_POST['qtdPaginas'];
		fwrite($file, $qtdPag);
	}
	fclose($file);
	
	$name = 'maxPagSementes.collect';
	$maxSementes = $_POST['maxPagSementes'];
	$file = fopen($name, 'w');
	$maxSementes = $maxSementes."\n";
	if ($maxSementes == 1){
		fwrite($file, $maxSementes);
		$qtdMax = $_POST['qtdMaxPaginas'];
		fwrite($file, $qtdMax);
	}else{
		fwrite($file, $maxSementes);
	}
	fclose($file);
	
	$name = 'heuLimSim.collect';
	$heuLimSim = $_POST['heuLimSim'];
	$heuLimSim = $heuLimSim."\n";
	$pesoGenero = $_POST['pesoGenero'];
	$pesoGenero = $pesoGenero."\n";
	$pesoConteudo = $_POST['pesoConteudo'];
	$pesoConteudo = $pesoConteudo."\n";
	$file = fopen($name, 'w');
	fwrite($file, $heuLimSim);
	fwrite($file, $pesoGenero);
	fwrite($file, $pesoConteudo);
	fclose($file);
	
	$name = 'pesos.collect';
	$pesoGenero = $_POST['pesoGenero'];
	$pesoGenero = $pesoGenero."\n";
	$pesoConteudo = $_POST['pesoConteudo'];
	$pesoConteudo = $pesoConteudo."\n";
	$pesoGeneroConteudo = $_POST['pesoGeneroConteudo'];
	$pesoGeneroConteudo = $pesoGeneroConteudo."\n";
	$pesoURL = $_POST['pesoURL'];
	$pesoURL = $pesoURL."\n";
	$file = fopen($name, 'w');
	fwrite($file, $pesoGenero);
	fwrite($file, $pesoConteudo);
	fwrite($file, $pesoGeneroConteudo);
	fwrite($file, $pesoURL);
	fclose($file);
	
	$name = 'linkContext.collect';
	$linkContext = $_POST['linkContext'];
	$file = fopen($name, 'w');
	fwrite($file, $linkContext);
	fclose($file);
		
	printf('oi'); 
	exec('java -jar focused-crawler-seq-1.0-SNAPSHOT-jar-with-dependencies.jar'); 
	printf('oi2');
} ?>

<body class="bgColor font">
	<form method="POST" action="">
		<div class="col-md-12">
			<div class="col-md-4 text-left"> 
				<img src="logo.png" style="width: 270px; height: 109px;">
			</div>
			<div class="col-md-12 text-center marginTitle">
				<p class="fontApresentacao styleApresentacao"> Bem vindo ao <span class="bold"> Yucca</span>!</p>
				<p class="fontApresentacao styleColeta"> Faça aqui a sua coleta temática</p>
			</div>
		</div>
        		
		<div class="col-md-6" style="text-align: center;">
            <p>Termos do gênero das páginas desejadas:</p><textarea class="textArea" placeholder="Digite aqui!" name="genero" rows="8" cols="45"></textarea><br />
        </div>
        <div class="col-md-6" style="text-align: center;">
            <p>Termos do conteúdo das páginas desejadas:</p><textarea class="textArea" name="conteudo" placeholder="Digite aqui!" rows="8" cols="45" style="margin-bottom: 30px;"></textarea><br />
        </div>
		
		<div class="col-md-12 text-right">
			<a href="javascript:showonlyonev2('container');"><p class="personalizarColeta"> Personalizar coleta </p></a>
		</div>
		
		<div class="newboxes-2" id="container" style="display:none">
			<div class="col-md-12 text-center newboxes-2" style="margin-left: 13%;">
			<div class="w3-container">
			  <div class="w3-card-4" style="width:74%;">
				<header class="w3-container colorHeader">
				  <p class="titleHeader">Configurações gerais</h1>
				</header>

				<div class="w3-container">
					<div style="text-align: center; margin-top: 20px">
						<p> Deseja informar o número máximo de páginas-sementes a serem coletadas? </p>
						<input name="maxPagSementes" id="maxPagSementes_sim" type="radio" value="1" onClick="InfoMaxPagSementes()"/>Sim
						<input name="maxPagSementes" id="maxPagSementes_nao" type="radio" value="0" onClick="InfoMaxPagSementes()"/>Não<br/><br/>	
						<p id="camposMaxQtd" style="display:none"> Informe a quantidade máxima de páginas-sementes coletadas: * <input name="qtdMaxPaginas" type="number" id="numberMaxPagSem" style="color: black;"> </p>
					</div>
					<div style="text-align: center;">
						<p> Informe o peso das palavras-chaves de gênero: <input class="inputStyle" name="pesoGenero" type="text"> </p>
						<p> Informe o peso das palavras-chaves de conteúdo: <input class="inputStyle" name="pesoConteudo" type="text"> </p>
						<p> Informe o peso das palavras-chaves de gênero e conteúdo: <input class="inputStyle" name="pesoGeneroConteudo" type="text"> </p>
						<p> Informe o peso da URL: <input class="inputStyle" name="pesoURL" type="text"> </p>
					</div>
				</div>
				<footer class="w3-container colorHeader">
				  <p id="helpCamposMaxQtd" style="display:none" class="styleFooter">* Para infinitas páginas, digite -1.</h5>
				</footer>
			  </div>
			</div>
			
			<div class="w3-container" style="margin-top: 52px;">
			  <div class="w3-card-4" style="width:74%;background-color: #201144;">
				<header class="w3-container colorHeader">
				  <p class="titleHeader">Configurações da geração semi-automática de páginas-semente </h1>
				</header>
				<div class="w3-container">
					<div style="text-align: center;">
						<p> Deseja informar as páginas-sementes? </p>
						<input name="pagSementes" id="pagSementes_sim" type="radio" value="1" onClick="InfoPagSementes()"/>Sim
						<input name="pagSementes" id="pagSementes_nao" type="radio" value="0" onClick="InfoPagSementes()"/>Não<br/><br/>
						
						<p id="camposQtd" style="display:none"> Informe a quantidade de páginas-sementes automáticas: <input class="inputStyle" name="qtdPaginas" type="number" id="myNumber" style="color: black;"> </p>
						<p id="campos" style="display:none"><textarea class="textArea" name="pagSemente" placeholder="Digite aqui!" rows="8" cols="45"></textarea></p>
					</div>
				</div>
			  </div>
			</div>
			
			
			<div class="w3-container" style="margin-top: 52px;">
			  <div class="w3-card-4" style="width:74%;background-color: #201144;">
				<header class="w3-container colorHeader">
				  <p class="titleHeader">Configurações da determinação automática do limite de similaridade </h1>
				</header>
				<div class="w3-container">
					<div style="text-align: center;">
						<p> Deseja utilizar qual heurística para a determinação automática do limite de similaridade? </p>
						<input name="heuLimSim" id="heuLimSim_k" type="radio" value="0"/> K-means <br/>
						<input name="heuLimSim" id="heuLimSim_cs" type="radio" value="2"/> Coeficiente de silhueta<br/>
						<input name="heuLimSim" id="heuLimSim_m" type="radio" value="3"/> Média aritmética das similaridades<br/><br/>
					</div>
				</div>
				<footer class="w3-container colorHeader">
				  <p class="styleFooter">- K-Means é um algoritmo de classificação dos métodos de particionamento, onde a geração dos clusters 
				  é realizada utilizando técnicas de distância entre os dados pertencentes a um n-dimensional, onde n é o número 
				  de atributos existentes na base de dados.</p>
				  
				  <p class="styleFooter">- Coeficiente de silhueta utiliza a maximização da métrica coeficiente de silhueta 
				  em clusters formados, por páginas relevantes e não relevantes ao tópico em questão, após a realização de um processo
				  de coleta para calcular o valor do limite de similaridade.
				  Esses clusters são formados baseado no valor de relevância de cada instância em
				  relação à um limite de similaridade. </p>
				  
				  <p class="styleFooter">- Média aritmética utiliza média aritmética ou ponderada das similaridades das páginas-semente,
oriundas da sumarização de dados para calcular o limite de similaridade.</p>
				</footer>
			  </div>
			</div>
			
		</div>
		</div>		
		
        <div style="text-align: center;">
            <input type="submit" name="button" class="button" value="Iniciar coleta!" style="margin-top: 35px;">
        </div>
    </form>
</body>
</html>