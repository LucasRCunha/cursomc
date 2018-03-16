package com.lucas.cursomc.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lucas.cursomc.domain.ItemPedido;
import com.lucas.cursomc.domain.PagamentoComBoleto;
import com.lucas.cursomc.domain.Pedido;
import com.lucas.cursomc.domain.enums.EstadoPagamento;
import com.lucas.cursomc.repositories.ClienteRepository;
import com.lucas.cursomc.repositories.ItemPedidoRepository;
import com.lucas.cursomc.repositories.PagamentoRepository;
import com.lucas.cursomc.repositories.PedidoRepository;
import com.lucas.cursomc.repositories.ProdutoRepository;
import com.lucas.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository repo;
	@Autowired
	private BoletoService boletoService;
	@Autowired
	private PagamentoRepository pagamentoRepository;
	@Autowired
	private ProdutoRepository podutoRepository;
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	@Autowired
	private ClienteRepository clienteRepository;
	
	public Pedido find(Integer id) {
		Pedido obj = repo.findOne(id);
		if(obj==null) {
			throw new ObjectNotFoundException("Objeto não encontrado! Id: "+id+", Tipo "+Pedido.class.getName());
		}
		return obj;	
	}
	
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteRepository.findOne(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pgto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pgto,obj.getInstante());
		}
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for(ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(podutoRepository.findOne(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
		}
		itemPedidoRepository.save(obj.getItens());
		System.out.println(obj);
		return obj;
	}
}
