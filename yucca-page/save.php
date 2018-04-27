<?php 
error_reporting(E_ALL); ini_set('display_errors', '1');

$conteudo = $_POST['conteudo'];
$genero = $_POST['genero'];
$pagSemente = $_POST['pagSemente'];

$file_genero = fopen("test_genre.collect","w");
fwrite($file_genero, $genero);
fclose($file_genero);

$file_conteudo = fopen("test_content.collect","w");
fwrite($file_conteudo, $conteudo);
fclose($file_conteudo);


$file_paginasSemente = fopen("test_seeds.fcrawler","w");
fwrite($file_paginasSemente, $pagSemente);
fclose($file_paginasSemente);

?>
