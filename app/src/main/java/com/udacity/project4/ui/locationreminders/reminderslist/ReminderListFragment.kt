package com.udacity.project4.ui.locationreminders.reminderslist

import android.os.Bundle
import android.view.*
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    override val baseViewModel: RemindersListViewModel by viewModel()
    private val binding by lazy { FragmentRemindersBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            viewModel = baseViewModel
            lifecycleOwner = this@ReminderListFragment

            setHasOptionsMenu(true)
            setDisplayHomeAsUpEnabled(false)
            setTitle(getString(R.string.app_name))
            setupRecyclerView()

            refreshLayout.setOnRefreshListener { baseViewModel.loadReminders() }
            addReminderFAB.setOnClickListener {
                navigateToAddReminder()
            }
        }


    }

    override fun onResume() {
        super.onResume()
        baseViewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        baseViewModel.navigationCommand.postValue(
            NavigationCommand.To(ReminderListFragmentDirections.toSaveReminder())
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter()
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.authenticationActivity -> AuthUI.getInstance().signOut(requireContext())
        }
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        ) || super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

}
